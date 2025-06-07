"use client";

import React from "react";
import { format, parseISO } from "date-fns";
import { AudioLines, Box as Film3D, Calendar, Clock, Subtitles, Ticket, Pin, MapPin, TvMinimal } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Session, Seat, SeatType, SessionDetailed } from "@/lib/types";

interface BookingSummaryProps {
  session: SessionDetailed;
  selectedSeats: Seat[];
  onCheckout: () => void;
  isLoading: boolean;
}

const BookingSummary: React.FC<BookingSummaryProps> = ({
  session,
  selectedSeats,
  onCheckout,
  isLoading,
}) => {
  if (!session) return null;

  const startTime = parseISO(session.startTime);
  const endTime = parseISO(session.endTime);

  const formatTime = (date: Date) => {
    return format(date, "h:mm a");
  };

  const getSeatPrice = (seat: Seat) => {
    switch (seat.seatType) {
      case SeatType.STANDARD:
        return session.standardSeatPrice;
      case SeatType.VIP:
        return session.vipSeatPrice;
      case SeatType.PWD:
        return session.pwdSeatPrice;
      default:
        return session.standardSeatPrice;
    }
  };

  const calculateTotalPrice = () => {
    return selectedSeats.reduce((total, seat) => total + getSeatPrice(seat), 0);
  };

  const capitalize = (text: string) => {
    return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
  }

  const groupedSeats = selectedSeats.reduce<{ [key: string]: Seat[] }>(
    (groups, seat) => {
      const type = seat.seatType;
      if (!groups[type]) {
        groups[type] = [];
      }
      groups[type].push(seat);
      return groups;
    },
    {}
  );

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>Booking Summary</CardTitle>
        <CardDescription>
          Review your selection before checkout
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div>
          <h3 className="font-semibold text-lg">{session.movie?.title}</h3>
          <div className="flex flex-col text-sm text-muted-foreground mt-2 space-y-1">
            <div className="flex items-center">
              <Calendar className="h-4 w-4 mr-2" />
              {format(startTime, "EEEE, MMMM d, yyyy")}
            </div>
            <div className="flex items-center">
              <Clock className="h-4 w-4 mr-2" />
              {formatTime(startTime)} - {formatTime(endTime)}
            </div>
            <div className="flex items-center">
              <AudioLines className="h-4 w-4 mr-2" />
              {capitalize(session.audioLanguage)}
            </div>
            {session.hasSubtitles && (
              <div className="flex items-center">
                <Subtitles className="h-4 w-4 mr-2" />
                Subtitled
              </div>
            )}
            {session.threeD && (
              <div className="flex items-center">
                <Film3D className="h-4 w-4 mr-2" />
                3D
              </div>
            )}
            <div className="flex items-center">
              <Ticket className="h-4 w-4 mr-2" />
              {session.theaterName} • {session.screenName}
            </div>
            {/* <div className="flex items-center">
              <MapPin className="h-4 w-4 mr-2" />
              {session.theaterAddress}
            </div> */}
          </div>
        </div>

        <Separator />

        <div>
          <h4 className="font-medium mb-2">Selected Seats</h4>
          {selectedSeats.length > 0 ? (
            <div className="space-y-3">
              {Object.entries(groupedSeats).map(([type, seats]) => (
                <div key={type} className="flex justify-between text-sm">
                  <div>
                    {seats.length} x {type} ({seats.map(s => `${s.seatRow}${s.seatNumber}`).join(", ")})
                  </div>
                  <div className="font-medium">
                    R${(getSeatPrice(seats[0]) * seats.length).toFixed(2)}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No seats selected</p>
          )}
        </div>

        <Separator />

        <div className="flex justify-between font-semibold">
          <div>Total</div>
          <div>R${calculateTotalPrice().toFixed(2)}</div>
        </div>
      </CardContent>
      <CardFooter>
        <Button
          className="w-full"
          disabled={selectedSeats.length === 0 || isLoading}
          onClick={onCheckout}
        >
          {isLoading ? "Processing..." : "Proceed to Checkout"}
        </Button>
      </CardFooter>
    </Card>
  );
};

export default BookingSummary;