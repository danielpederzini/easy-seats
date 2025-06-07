"use client";

import React from "react";
import { format, parseISO } from "date-fns";
import { ChevronDown, ChevronUp, Calendar, Clock, Ticket, MapPin, TvMinimal, AudioLines, Subtitles, Box as Film3D, CalendarClock, Hash } from "lucide-react";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { BookingDetailed, BookingStatus, SeatType } from "@/lib/types";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import CountdownTimer from "./CountdownTimer";
import { string } from "zod";

interface BookingCardProps {
  booking: BookingDetailed;
  cancelBooking: (bookingId: number) => void;
  createQrCode: (bookingId: number) => void;
}

const BookingCard: React.FC<BookingCardProps> = ({ booking, cancelBooking, createQrCode }) => {
  const [isOpen, setIsOpen] = React.useState(false);

  const formatDate = (dateString: string) => {
    try {
      return format(parseISO(dateString), "PPP");
    } catch (error) {
      return dateString;
    }
  };

  const formatTime = (dateString: string) => {
    try {
      return format(parseISO(dateString), "h:mm a");
    } catch (error) {
      return dateString;
    }
  };

  const formatDateTime = (dateString: string) => {
    try {
      return format(parseISO(dateString), "PPP h:mm a");
    } catch (error) {
      return dateString;
    }
  };

  const capitalize = (text: string) => {
    return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
  }

  const getStatusBadge = (booking: BookingDetailed) => {
    switch (booking.bookingStatus) {
      case BookingStatus.PAYMENT_CONFIRMED:
        return <Badge className="bg-green-500">Confirmed</Badge>;
      case BookingStatus.AWAITING_PAYMENT:
        return <Badge className="bg-yellow-500">{booking.checkoutCompleted ? "Awaiting Confirmation" : "Pending"}</Badge>;
      case BookingStatus.PAYMENT_RETRY:
        return <Badge className="bg-yellow-500">Payment Failed - Try Again</Badge>;
      case BookingStatus.PAST:
        return <Badge className="text-muted-foreground bg-neutral-700">Past</Badge>;
      case BookingStatus.AWAITING_CANCELLATION:
        return <Badge className="text-muted-foreground bg-neutral-700">Awaiting Refund</Badge>;
      case BookingStatus.CANCELLED:
        return <Badge className="text-muted-foreground bg-neutral-700">Refunded</Badge>;
      default:
        return <Badge>{status}</Badge>;
    }
  };

  const getSeatTypeColor = (type: SeatType) => {
    switch (type) {
      case SeatType.VIP:
        return "text-amber-400";
      case SeatType.PWD:
        return "text-blue-400";
      default:
        return "text-white";
    }
  };

  return (
    <Card className="overflow-hidden transition-all pr-6 pl-6 pt-3 pb-3 duration-200 hover:shadow-md">
      <Collapsible open={isOpen} onOpenChange={setIsOpen}>
        <CardHeader className="pb-3">
          <div className="flex justify-between items-center">
            <CardTitle className="text-lg">
              {booking.movie.title}
            </CardTitle>
            <div className="flex-col items-center space-x-2">
              {getStatusBadge(booking)}
            </div>
          </div>
        </CardHeader>
        <CardContent className="pb-3">
          <div className="flex flex-row justify-between gap-4">
            <div className="space-y-2">
              <div className="flex items-center text-sm text-muted-foreground">
                <CalendarClock className="h-4 w-4 mr-2" />
                {formatDate(booking.session.startTime)} • {formatTime(booking.session.startTime)}
              </div>
              <div className="flex items-center text-sm text-muted-foreground">
                <Ticket className="h-4 w-4 mr-2" />
                {booking.session.theaterName} • {booking.session.screenName}
              </div>
            </div>
            <div className="flex items-end flex-col justify-start mt-2">
              <div className="text-right flex-col">
                <div className="text-sm text-muted-foreground">Total</div>
                <div className="font-semibold">R${booking.totalPrice.toFixed(2)}</div>
                {(booking.bookingStatus === BookingStatus.AWAITING_PAYMENT || booking.bookingStatus === BookingStatus.PAYMENT_RETRY) && !booking.checkoutCompleted && (
                  <div className="text-sm">
                    <CountdownTimer targetDate={booking.expiresAt} fixedColor={"text-yellow-500"} />
                  </div>
                )}
              </div>
              <div className="flex flex-row items-center justify-between flex-nowrap gap-4">
                {(booking.bookingStatus === BookingStatus.AWAITING_PAYMENT || booking.bookingStatus === BookingStatus.PAYMENT_RETRY) && (
                  <Button
                    variant="outline"
                    className="w-full mt-6"
                    onClick={() => window.open(booking.checkoutUrl, "_blank", "noopener,noreferrer")}
                  >
                    Go to Payment
                  </Button>
                )}
                {booking.bookingStatus === BookingStatus.PAYMENT_CONFIRMED && (
                  <Button
                    variant="outline"
                    className="w-full mt-6"
                    onClick={() => createQrCode(booking.id)}
                  >
                    View QRCode
                  </Button>
                )}
              </div>
            </div>
          </div>
        </CardContent>

        <CollapsibleTrigger asChild>
            <Button variant="ghost" size="sm" className="w-full mt-6 mb-2">
              {isOpen ? (
                <span className="flex items-center">
                  Show Less <ChevronUp className="h-4 w-4 ml-1" />
                </span>
              ) : (
                <span className="flex items-center">
                  Show Details <ChevronDown className="h-4 w-4 ml-1" />
                </span>
              )}
            </Button>
          </CollapsibleTrigger>

        <CollapsibleContent>
          <CardContent className="pt-8 border-t pb-8 overflow-hidden transition-all duration-300">
            <div className="space-y-8">
              <div>
                <h4 className="text-sm font-medium mb-2">Session Details</h4>
                <div className="space-y-2 pt-2">
                  <div className="flex items-center text-sm text-muted-foreground">
                    <AudioLines className="h-4 w-4 mr-2" />
                    {capitalize(booking.session?.audioLanguage)}
                  </div>
                </div>
                {booking.session.hasSubtitles && (<div className="space-y-2 pt-2">
                  <div className="flex items-center text-sm text-muted-foreground">
                    <Subtitles className="h-4 w-4 mr-2" />
                    Subtitled
                  </div>
                </div>)}
                {booking.session.threeD && (<div className="space-y-2 pt-2">
                  <div className="flex items-center text-sm text-muted-foreground">
                    <Film3D className="h-4 w-4 mr-2" />
                    3D
                  </div>
                </div>)}
              </div>

              <div>
                <h4 className="text-sm font-medium mb-2">Booking Details</h4>
                <div className="space-y-2 pt-2">
                  <div className="flex items-center text-sm text-muted-foreground">
                    <Hash className="h-4 w-4 mr-2" />
                    ID: {booking.id}
                  </div>
                </div>
                <div className="space-y-2 pt-2">
                  <div className="flex items-center text-sm text-muted-foreground">
                    <CalendarClock className="h-4 w-4 mr-2" />
                    {formatDateTime(booking.createdAt)}
                  </div>
                </div>
              </div>

              <div>
                <h4 className="text-sm font-medium mb-2">Booked Seats</h4>
                <div className="grid grid-cols-3 md:grid-cols-2 lg:grid-cols-2 2xl:grid-cols-3 gap-2 pt-2">
                  {booking.bookedSeats.map((bookedSeat) => (
                    <div
                      key={bookedSeat.id}
                      className="bg-muted/50 rounded-md text-sm p-3"
                    >
                      <div className="flex justify-between">
                        <span>
                          {bookedSeat.seatRow}
                          {bookedSeat.seatNumber}
                        </span>
                        <div className={`text-xs ${getSeatTypeColor(bookedSeat.seatType)}`}>
                          {bookedSeat.seatType != SeatType.STANDARD ? bookedSeat.seatType : "STND"}
                        </div>

                      </div>
                      <span className="text-xs text-muted-foreground">
                        R${bookedSeat.seatPrice.toFixed(2)}
                      </span>

                    </div>
                  ))}
                </div>
              </div>
            </div>
          </CardContent>
          {booking.bookingStatus === BookingStatus.PAYMENT_CONFIRMED && (
            <CardFooter className="border-t pt-6">
              <Button
                variant="outline"
                className="w-full hover:bg-red-500 hover:text-white"
                onClick={() => cancelBooking(booking.id)}
              >
                Cancel and Refund
              </Button>
            </CardFooter>
          )}
        </CollapsibleContent>
      </Collapsible>
    </Card>
  );
};

export default BookingCard;