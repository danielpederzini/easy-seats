"use client";

import React, { useEffect, useState } from "react";
import { usePathname, useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import { CheckCircle, ArrowRight, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/context/auth-context";
import { tryConfirmingPayment } from "@/lib/api/bookings";
import { ApiError } from "next/dist/server/api-utils";
import { toast } from "@/hooks/use-toast";

export default function PaymentSuccessPage() {
  const pathName = usePathname();
  const searchParams = useSearchParams();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState("");
  const { isAuthenticated, isLoading: authLoading } = useAuth();

  const bookingId = searchParams.get("bookingId");
  const checkoutId = searchParams.get("checkoutId");

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      redirectToAuth();
      return;
    }

    if (bookingId && checkoutId && paymentStatus === "") {
      tryConfirmingBooking();
    }
  }, [isAuthenticated, authLoading, router, bookingId, checkoutId]);

  const tryConfirmingBooking = async () => {
    if (loading) return;
    setLoading(true);
    try {
      setTimeout(() => 5000); // 5 second delay

      const confirmed = await tryConfirmingPayment(Number(bookingId), checkoutId);
      if (confirmed) {
        setPaymentStatus("confirmed");
        setLoading(false);
      } else {
        setPaymentStatus("pending");
        setLoading(false);
      }
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if ((status === 401 || status === 403) && err.message === "refresh-error") {
        redirectToAuth();
        return;
      }

      setPaymentStatus("unknown");
      setLoading(false);
      toast({
        title: "Error checking payment confirmation",
        description: "Check your bookings page to see your payment status.",
        variant: "destructive",
        bgColor: "bg-yellow-500"
      });
    }
  }

  const redirectToAuth = () => {
    const params = searchParams.toString();
    const redirectUrl = params ? `${pathName}?${params}` : pathName;
    router.push(`/auth/login?redirect=${encodeURIComponent(redirectUrl)}`);
  }

  // Render based on paymentStatus
  if (loading || paymentStatus === "") {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (paymentStatus === "confirmed") {
    return (
      <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
        <div className="container flex items-center justify-center min-h-[calc(100vh-4rem)] py-12">
          <Card className="w-full max-w-md shadow-lg">
            <CardHeader className="pb-4 text-center">
              <div className="mx-auto w-16 h-16 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center mb-4">
                <CheckCircle className="h-10 w-10 text-green-600 dark:text-green-400" />
              </div>
              <CardTitle className="text-2xl">Payment Successful!</CardTitle>
              <CardDescription>
                Your booking has been confirmed and your tickets are ready.
              </CardDescription>
            </CardHeader>
            <CardContent className="text-center">
              <p className="text-muted-foreground mb-4">
                Thank you for your purchase. Your booking reference is:
              </p>
              <div className="bg-muted p-3 rounded-md font-mono font-semibold text-lg mb-4">
                {bookingId}
              </div>
              <p className="text-sm text-muted-foreground">
                We've sent the booking details to your email. You can also view your booking in your account.
              </p>
            </CardContent>
            <CardFooter className="flex flex-col space-y-2">
              <Button asChild className="w-full">
                <Link href="/bookings">
                  View My Bookings <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
              <Button variant="outline" asChild className="w-full">
                <Link href="/movies">
                  Browse More Movies
                </Link>
              </Button>
            </CardFooter>
          </Card>
        </div>
      </section>
    );
  }

  if (paymentStatus === "pending") {
    return (
      <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
        <div className="container flex items-center justify-center min-h-[calc(100vh-4rem)] py-12">
          <Card className="w-full max-w-md shadow-lg">
            <CardHeader className="pb-4 text-center">
              <div className="mx-auto w-16 h-16 rounded-full bg-yellow-100 dark:bg-yellow-900/30 flex items-center justify-center mb-4">
                <CheckCircle className="h-10 w-10 text-yellow-600 dark:text-yellow-400" />
              </div>
              <CardTitle className="text-2xl">Payment Pending</CardTitle>
              <CardDescription>
                We couldn't confirm your payment yet. Please check your bookings page later.
              </CardDescription>
            </CardHeader>
            <CardContent className="text-center">
              <p className="text-muted-foreground mb-4">
                Your booking reference is:
              </p>
              <div className="bg-muted p-3 rounded-md font-mono font-semibold text-lg mb-4">
                {bookingId}
              </div>
              <p className="text-sm text-muted-foreground">
                If you have been charged but don't see your booking, please contact support.
              </p>
            </CardContent>
            <CardFooter className="flex flex-col space-y-2">
              <Button asChild className="w-full">
                <Link href="/bookings">
                  View My Bookings <ArrowRight className="ml-2 h-4 w-4" />
                </Link>
              </Button>
              <Button variant="outline" asChild className="w-full">
                <Link href="/movies">
                  Browse More Movies
                </Link>
              </Button>
            </CardFooter>
          </Card>
        </div>
      </section>
    );
  }

  // Unknown or error
  return (
    <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
      <div className="container flex items-center justify-center min-h-[calc(100vh-4rem)] py-12">
        <Card className="w-full max-w-md shadow-lg">
          <CardHeader className="pb-4 text-center">
            <div className="mx-auto w-16 h-16 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center mb-4">
              <CheckCircle className="h-10 w-10 text-red-600 dark:text-red-400" />
            </div>
            <CardTitle className="text-2xl">Payment Status Unknown</CardTitle>
            <CardDescription>
              We couldn't confirm your payment. Please check your bookings page or contact support.
            </CardDescription>
          </CardHeader>
          <CardContent className="text-center">
            <p className="text-muted-foreground mb-4">
              Your booking reference is:
            </p>
            <div className="bg-muted p-3 rounded-md font-mono font-semibold text-lg mb-4">
              {bookingId}
            </div>
            <p className="text-sm text-muted-foreground">
              If you have been charged but don't see your booking, please contact support.
            </p>
          </CardContent>
          <CardFooter className="flex flex-col space-y-2">
            <Button asChild className="w-full">
              <Link href="/bookings">
                View My Bookings <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
            <Button variant="outline" asChild className="w-full">
              <Link href="/movies">
                Browse More Movies
              </Link>
            </Button>
          </CardFooter>
        </Card>
      </div>
    </section>
  );
}