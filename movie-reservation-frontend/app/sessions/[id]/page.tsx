"use client";

import React, { useState, useEffect, useCallback } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { ChevronLeft, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";
import { useSeatWebSocket } from "@/hooks/use-websocket";
import SeatMap from "@/components/sessions/SeatMap";
import BookingSummary from "@/components/sessions/BookingSummary";
import { getSession, tryReserveSeatsInCache, releaseSeatsFromCache } from "@/lib/api/sessions";
import { createBooking } from "@/lib/api/bookings";
import { SessionDetailed, Seat, SeatUpdate } from "@/lib/types";
import { useAuth } from "@/context/auth-context";
import { ApiError } from "next/dist/server/api-utils";
import CountdownTimer from "@/components/bookings/CountdownTimer";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface SeatSelectionPageProps {
  params: {
    id: number;
  }
}

const SeatSelectionPage: React.FC<SeatSelectionPageProps> = ({
  params
}) => {
  const pathName = usePathname();
  const searchParams = useSearchParams();
  const sessionId = Number(params.id);
  const ttl = 5 * 60 * 1000;
  const router = useRouter();
  const { toast } = useToast();
  const { isAuthenticated, isLoading: authLoading } = useAuth();

  const [loading, setLoading] = useState(true);
  const [failedModalOpen, setFailedModalOpen] = useState(false);
  const [expireModalOpen, setExpiredModalOpen] = useState(false);
  const [processingBooking, setProcessingBooking] = useState(false);
  const [session, setSession] = useState<SessionDetailed | null>(null);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([]);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      redirectToAuth();
      return;
    }
  }, [isAuthenticated, authLoading, router]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        const sessionDetailed = await getSession(params.id);

        setSession(sessionDetailed);
        setSeats(sessionDetailed.seats || []);
      } catch (err: ApiError | any) {
        const status = err.statusCode;

        if ((status === 401 || status === 403) && err.message === "refresh-error") {
          redirectToAuth();
          return;
        }

        var description = "Something went wrong. Please try again later.";

        if (status === 410) {
          description = "This session is no longer accepting bookings.";
        } else if (status === 500) {
          description = "Something went wrong on the server. Please try again later.";
        } else if (status) {
          description = `Failed with status ${status}. Please try again later.`;
        }

        toast({
          title: "Error loading session data.",
          description: description,
          variant: "destructive",
          bgColor: "bg-red-500"
        });
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [sessionId, toast]);

  const handleSeatUpdate = useCallback((update: SeatUpdate) => {
    setSeats(prev => prev.map(seat =>
      seat.id === update.id ? { ...seat, taken: update.taken } : seat
    ));

    if (update.originId === "redis-expiration-listener") {
      setSelectedSeats(prev => prev.filter(seat => seat.id !== update.id));
    }
  }, []);

  const wsStatus = useSeatWebSocket(sessionId, ttl, handleSeatUpdate);

  useEffect(() => {
    if (wsStatus === "expired") {
      setExpiredModalOpen(true);
    } else if (wsStatus === "connection-failed") {
      setFailedModalOpen(true);
    }
  }, [wsStatus])

  const handleSeatSelect = async (seat: Seat) => {
    const isSelected = selectedSeats.some((s) => s.id === seat.id);

    if (isSelected) {
      releaseSeat(seat);
    } else {
      if (selectedSeats.length >= 5) {
        toast({
          title: "Seat limit reached",
          description: "You can only select up to 5 seats.",
          variant: "destructive",
          bgColor: "bg-red-500"
        });
        return;
      }

      reserveSeat(seat);
    }
  };

  const releaseSeat = async (seat: Seat) => {
    try {
      await releaseSeatsFromCache(sessionId, seat.id);
      setSelectedSeats((prev) => prev.filter((s) => s.id !== seat.id));
    } catch (err: ApiError | any) {
      const status = err.statusCode;
      var description = "Something went wrong. Please try again later.";

      if (status === 500) {
        description = "Something went wrong on the server. Please try again later.";
      } else if (status) {
        description = `Failed with status ${status}. Please try again later.`;
      }

      toast({
        title: "Failed to deselect seat",
        description: description,
        variant: "destructive",
        bgColor: "bg-red-500"
      });
    }
  }

  const reserveSeat = async (seat: Seat) => {
    try {
      await tryReserveSeatsInCache(sessionId, seat.id);
      setSelectedSeats((prev) => [...prev, seat]);
    } catch (err: ApiError | any) {
      const status = err.statusCode;
      var description = "Something went wrong. Please try again later.";

      if (status === 409) {
        description = "This seat is already taken. Please select another seat.";
      } else if (status === 500) {
        description = "Something went wrong on the server. Please try again later.";
      } else if (status) {
        description = `Failed with status ${status}. Please try again later.`;
      }

      toast({
        title: "Failed to select seat",
        description: description,
        variant: "destructive",
        bgColor: "bg-red-500"
      });

    }
  }

  const handleCheckout = async () => {
    if (selectedSeats.length === 0) {
      toast({
        title: "No seats selected",
        description: "Please select at least one seat to continue.",
        variant: "destructive",
        bgColor: "bg-red-500"
      });
      return;
    }

    try {
      setProcessingBooking(true);
      setLoading(true);

      const bookingResponse = await createBooking(
        sessionId,
        selectedSeats.map((seat) => seat.id),
        `http://localhost:3000/bookings/success`, // Success URL
        `http://localhost:3000/bookings/` // Cancel URL
      );

      router.push(bookingResponse.checkoutUrl);
    } catch (err: ApiError | any) {
      console.log(err)

      const status = err.statusCode;
      var description = "Something went wrong. Please try again later.";

      if (status === 410) {
        description = "This session is no longer accepting bookings.";
      } else if (status === 500) {
        description = "Something went wrong on the server. Please try again later.";
      } else if (status) {
        description = `Failed with status ${status}. Please try again later.`;
      }

      toast({
        title: "Failed to create booking",
        description: description,
        variant: "destructive",
        bgColor: "bg-red-500"
      });

      setProcessingBooking(false);
      setLoading(false);
    }
  };

  const getFormattedTtl = () => {
    return ttl < 60000 ? `${Math.round(ttl / 1000)} seconds` : `${(ttl / 60000).toFixed(0)} minute${(ttl / 60000) === 1 ? "" : "s"}`
  }

  const sendBackToSessions = () => {
    router.push(`/movies/${session?.movie.id}/sessions`);
  }

  const redirectToAuth = () => {
    const params = searchParams.toString();
    const redirectUrl = params ? `${pathName}?${params}` : pathName;
    router.push(`/auth/login?redirect=${encodeURIComponent(redirectUrl)}`);
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!session) {
    return (
      <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
        <div className="container py-8">
          <div className="text-center py-12">
            <p className="text-muted-foreground">Session not found.</p>
            <Button asChild className="mt-4" onClick={sendBackToSessions}>
              <Link href="/movies">Back to Movies</Link>
            </Button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <>
      <Dialog open={failedModalOpen} onOpenChange={(open) => {
        if (!open) {
          sendBackToSessions();
        }
      }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Connection error</DialogTitle>
          </DialogHeader>
          <div className="py-4 text-muted-foreground text-center">
            Couldn't connect to the seat reservation server.
            Please go back to sessions and try again later.
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              className="w-full"
              onClick={sendBackToSessions}
            >
              Back to Sessions
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>


      <Dialog open={expireModalOpen} onOpenChange={(open) => {
        if (!open) {
          router.push(`/movies/${session?.movie.id}/sessions`);
        }
      }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Session Expired</DialogTitle>
          </DialogHeader>
          <div className="py-4 text-muted-foreground text-center">
            Your seat selection session expired after {getFormattedTtl()}.<br />
            Please go back to sessions to try again.
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              className="w-full"
              onClick={() => router.push(`/movies/${session?.movie.id}/sessions`)}
            >
              Back to Sessions
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
        <div className="container py-8">
          <Button variant="ghost" asChild className="mb-6">
            <Link href={`/movies/${session.movie.id}/sessions`}>
              <ChevronLeft className="mr-2 h-4 w-4" />
              Back to Sessions
            </Link>
          </Button>

          <div className="flex flex-row items-center justify-between mb-8">
            <div>
              <div className="flex items-center gap-4 mb-2">
                <img
                  src={session.movie.posterUrl || "https://images.pexels.com/photos/33129/popcorn-movie-party-entertainment.jpg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"}
                  alt={`${session.movie.title} logo`}
                  className="h-8 w-8 object-contain rounded-xl"
                />
                <h1 className="text-2xl font-bold">{session.movie.title}</h1>
              </div>
              <p className="text-muted-foreground">Select your seats</p>
            </div>

            <div className="text-md">
              <CountdownTimer targetDate={new Date(Date.now() + ttl)} fixedColor={null} />
            </div>
          </div>



          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 bg-black">
              <SeatMap
                seats={seats}
                session={session}
                selectedSeats={selectedSeats}
                onSeatSelect={handleSeatSelect}
              />
            </div>

            <div>
              <BookingSummary
                session={session}
                selectedSeats={selectedSeats}
                onCheckout={handleCheckout}
                isLoading={processingBooking}
              />
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

export default SeatSelectionPage;