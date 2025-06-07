"use client";

import React, { useState, useEffect } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { BookingStatus } from "@/lib/types";
import { useAuth } from "@/context/auth-context";
import { toast } from "@/hooks/use-toast";
import { ApiError } from "next/dist/server/api-utils";
import { createQrCode, tryCancelBooking } from "@/lib/api/bookings";
import BookingList from "@/components/bookings/BookingList";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

export default function BookingsPage() {
  const pathName = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [loading, setLoading] = useState(false);

  const [filters, setFilters] = useState<{ status: string }>({ status: "all" });
  const [qrModalOpen, setQrModalOpen] = useState(false);
  const [qrImageUrl, setQrImageUrl] = useState<string | null>(null);
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [bookingToCancel, setBookingToCancel] = useState<number | null>(null);

  useEffect(() => {
    // Redirect if not authenticated
    if (!authLoading && !isAuthenticated) {
      redirectToAuth();
      return;
    }
  }, [isAuthenticated, authLoading, router]);

  const backToMovies = () => {
    router.push("/movies")
  }

  const handleStatusChange = (newStatus: string) => {
    setFilters({ status: newStatus });
  }

  const cancelBooking = async () => {
    if (!bookingToCancel) return;
    setLoading(true);
    try {
      await tryCancelBooking(bookingToCancel);

      toast({
        title: "Success",
        description: "Booking cancelation requested.",
        variant: "destructive",
        bgColor: "bg-green-500"
      });
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if ((status === 401 || status === 403) && err.message === "refresh-error") {
        redirectToAuth();
        return;
      }

      var description = "Something went wrong. Please try again later.";

      if (status === 500) {
        description = "Something went wrong on the server. Please try again later.";
      } else if (status) {
        description = `Failed with status ${status}.`;
      }

      toast({
        title: "Error cancelling booking",
        description: description,
        variant: "destructive",
        bgColor: "bg-red-500"
      });
    } finally {
      setCancelModalOpen(false);
      setBookingToCancel(null);
      setLoading(false);
    }
  }

  const createAndShowQrCode = async (bookingId: number) => {
    setLoading(true);
    try {
      const blob = await createQrCode(bookingId);
      const url = URL.createObjectURL(blob);
      setQrImageUrl(url);
      setQrModalOpen(true);
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if ((status === 401 || status === 403) && err.message === "refresh-error") {
        redirectToAuth();
        return;
      }

      var description = "Something went wrong. Please try again later.";

      if (status === 500) {
        description = "Something went wrong on the server. Please try again later.";
      } else if (status) {
        description = `Failed with status ${status}.`;
      }

      toast({
        title: "Error creating QRCode",
        description: description,
        variant: "destructive",
        bgColor: "bg-red-500"
      });
    } finally {
      setLoading(false);
    }
  }

  const redirectToAuth = () => {
    const params = searchParams.toString();
    const redirectUrl = params ? `${pathName}?${params}` : pathName;
    router.push(`/auth/login?redirect=${encodeURIComponent(redirectUrl)}`);
  }

  if (authLoading || (loading && isAuthenticated)) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <>
      <Dialog open={cancelModalOpen} onOpenChange={setCancelModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cancel Booking</DialogTitle>
          </DialogHeader>
          <div className="py-4 text-muted-foreground">
            Are you sure you want to cancel this booking?
            Your tickets will be invalidated and you will be refunded.
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelModalOpen(false)} disabled={loading}>
              No, go back
            </Button>
            <Button
              variant="destructive"
              onClick={cancelBooking}
              disabled={loading}
            >
              Yes, cancel booking
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* QR Code Modal */}
      <Dialog open={qrModalOpen} onOpenChange={(open) => {
        setQrModalOpen(open);
        if (!open && qrImageUrl) {
          URL.revokeObjectURL(qrImageUrl);
          setQrImageUrl(null);
        }
      }}>
        <DialogContent className="max-w-xs w-full flex flex-col items-center">
          <DialogHeader>
            <DialogTitle>Your QR Code</DialogTitle>
          </DialogHeader>
          {qrImageUrl && (
            <img src={qrImageUrl} alt="Booking QR Code" className="w-64 h-64 object-contain mb-6" />
          )}
          <p className="text-xs text-muted-foreground text-center">
            Show this QR code at the theater to validate your booking. This code is valid for 10 minutes
          </p>
        </DialogContent>
      </Dialog>


      <section className="flex items-center justify-center w-full h-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
        <div className="container py-8">
          <h1 className="text-3xl font-bold mb-6">My Bookings</h1>
          <div className="flex flex-col md:flex-row justify-between gap-4 mb-6">
            <Tabs
              defaultValue="all"
              value={filters.status}
              onValueChange={handleStatusChange}
              className="w-full"
            >
              <TabsList className="grid w-full grid-cols-5 gap-2">
                <TabsTrigger value="all">All</TabsTrigger>
                <TabsTrigger value={BookingStatus.AWAITING_PAYMENT}>Pending</TabsTrigger>
                <TabsTrigger value={BookingStatus.PAYMENT_CONFIRMED}>Confirmed</TabsTrigger>
                <TabsTrigger value={BookingStatus.CANCELLED}>Cancelled</TabsTrigger>
                <TabsTrigger value={BookingStatus.PAST}>Past</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
          <BookingList
            filters={filters}
            cancelBooking={(id) => { setBookingToCancel(id); setCancelModalOpen(true); }}
            createAndShowQrCode={createAndShowQrCode}
            backToMovies={backToMovies}
          />
        </div>
      </section>
    </>
  );
}