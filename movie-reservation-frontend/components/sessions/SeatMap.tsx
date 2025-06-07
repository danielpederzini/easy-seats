"use client";

import React, { useState, useEffect } from "react";
import { Monitor, Info, Loader2, TvMinimal } from "lucide-react";
import { cn } from "@/lib/utils";
import { Seat, SeatType, SessionDetailed } from "@/lib/types";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { Badge } from "@/components/ui/badge";

interface SeatMapProps {
  seats: Seat[];
  selectedSeats: Seat[];
  session: SessionDetailed;
  onSeatSelect: (seat: Seat) => void;
}

const SeatMap: React.FC<SeatMapProps> = ({
  seats,
  session,
  selectedSeats,
  onSeatSelect,
}) => {
  const [seatRows, setSeatRows] = useState<{ [key: string]: Seat[] }>({});
  const [uniqueRows, setUniqueRows] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const groupedSeats: { [key: string]: Seat[] } = {};

    seats.forEach((seat) => {
      if (!groupedSeats[seat.seatRow]) {
        groupedSeats[seat.seatRow] = [];
      }
      groupedSeats[seat.seatRow].push(seat);
    });

    // Sort seats within each row by seat number
    Object.keys(groupedSeats).forEach((row) => {
      groupedSeats[row].sort((a, b) => a.seatNumber - b.seatNumber);
    });

    setSeatRows(groupedSeats);
    setUniqueRows(Object.keys(groupedSeats).sort());
  }, [seats]);

  const isSeatSelected = (seat: Seat) => {
    return selectedSeats.some((s) => s.id === seat.id);
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

  const getSeatColor = (seat: Seat) => {
    if (seat.taken) return "bg-gray-300 dark:bg-gray-700 cursor-not-allowed opacity-50";

    if (isSeatSelected(seat)) {
      return "bg-primary text-primary-foreground";
    }

    switch (seat.seatType) {
      case SeatType.VIP:
        return "bg-amber-100 hover:bg-amber-200 dark:bg-amber-900/30 dark:hover:bg-amber-800/50 border-amber-300 dark:border-amber-700";
      case SeatType.PWD:
        return "bg-blue-100 hover:bg-blue-200 dark:bg-blue-900/30 dark:hover:bg-blue-800/50 border-blue-300 dark:border-blue-700";
      default:
        return "bg-background hover:bg-accent border-border";
    }
  };

  const onSeatSelectLoader = async (seat: Seat) => {
    setLoading(true);
    await onSeatSelect(seat);
    setLoading(false);
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center space-y-8 bg-muted/30 rounded-lg pt-8 pb-8 pr-2 pl-2">
      <div className="flex justify-center w-full mb-2 overflow-hidden rounded-lg">
        <div className="w-3/4 h-8 bg-muted flex items-center justify-center">
          <TvMinimal className="h-5 w-5 mr-2" />
          <span className="text-sm">Screen</span>
        </div>
      </div>

      <div className="flex items-center justify-center gap-4 text-sm mb-2">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-sm bg-background border border-border"></div>
          <span>Standard</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-sm bg-amber-100 dark:bg-amber-900/30 border border-amber-300 dark:border-amber-700"></div>
          <span>VIP</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-sm bg-blue-100 dark:bg-blue-900/30 border border-blue-300 dark:border-blue-700"></div>
          <span>PWD</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-sm bg-gray-300 dark:bg-gray-700 opacity-50"></div>
          <span>Taken</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded-sm bg-primary"></div>
          <span>Selected</span>
        </div>
      </div>

      <div className="grid gap-6">
        {uniqueRows.map((row) => (
          <div key={row} className="flex items-center gap-2">
            <div className="w-6 text-center font-medium">{row}</div>
            <div className="flex gap-2">
              {seatRows[row].map((seat) => (
                <TooltipProvider key={seat.id}>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <button
                        type="button"
                        className={cn(
                          "w-8 h-8 rounded-sm flex items-center justify-center text-xs border transition-colors",
                          getSeatColor(seat)
                        )}
                        onClick={() => !seat.taken && onSeatSelectLoader(seat)}
                        disabled={seat.taken}
                      >
                        {seat.seatNumber}
                      </button>
                    </TooltipTrigger>
                    <TooltipContent side="top" className="text-xs">
                      <div className="space-y-1">
                        <div>Seat {row}{seat.seatNumber}</div>
                        <div>Type: {seat.seatType}</div>
                        <div className="font-semibold">Price: R${getSeatPrice(seat).toFixed(2)}</div>
                        {seat.taken && <Badge variant="destructive">Already Booked</Badge>}
                      </div>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="flex items-center justify-center text-xs text-muted-foreground mt-4">
        <Info className="h-4 w-4 mr-1" />
        <span>Click on a seat to select/deselect it</span>
      </div>
    </div>
  );
};

export default SeatMap;