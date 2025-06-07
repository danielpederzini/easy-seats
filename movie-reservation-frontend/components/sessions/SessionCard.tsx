"use client";

import React from "react";
import Link from "next/link";
import { format, parseISO } from "date-fns";
import { Clock, Subtitles, AudioLines, Calendar, MapPin, Box as Film3d, TvMinimal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Session, Language } from "@/lib/types";

interface SessionCardProps {
  session: Session;
  movieTitle: string;
}

const SessionCard: React.FC<SessionCardProps> = ({ session, movieTitle }) => {
  const startTime = parseISO(session.startTime);
  const endTime = parseISO(session.endTime);

  const formatTime = (date: Date) => {
    return format(date, "h:mm a");
  };

  return (
    <Card className="overflow-hidden transition-all duration-200 hover:shadow-md">
      <CardContent className="p-8">
        <div className="flex flex-col justify-between items-start gap-4">
          <div className="flex row-auto">
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <h3 className="font-semibold">{movieTitle}</h3>
                <Badge variant="outline" className="bg-black-500/10 text-black-500 border-black-500">
                  <AudioLines className="h-3 w-3 mr-1" />
                  {session.audioLanguage}
                </Badge>
                {session.hasSubtitles && (
                  <Badge variant="outline" className="bg-black-500/10 text-black-500 border-black-500">
                    <Subtitles className="h-3 w-3 mr-1" />
                    SUBTITLED
                  </Badge>
                )}
                {session.threeD && (
                  <Badge variant="outline" className="bg-black-500/10 text-black-500 border-black-500">
                    <Film3d className="h-3 w-3 mr-1" />
                    3D
                  </Badge>
                )}
              </div>
              <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
                <div className="flex items-center">
                  <Calendar className="h-4 w-4 mr-1" />
                  {format(startTime, "MMM dd, yyyy")}
                </div>
                <div className="flex items-center">
                  <Clock className="h-4 w-4 mr-1" />
                  {formatTime(startTime)} - {formatTime(endTime)}
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
                <div className="flex items-center">
                  <MapPin className="h-4 w-4 mr-1" />
                  {session.theaterName} - {session.screenName}
                </div>
              </div>
            </div>
          </div>
          <div className="flex flex-row items-center justify-between w-full mt-8">
            <div className="flex flex-row items-center gap-2">
              <div className="grid grid-cols-3 gap-4 text-center text-sm">
                <div className="flex flex-col">
                  <span className="text-muted-foreground">Standard</span>
                  <span className="font-semibold">R${session.standardSeatPrice.toFixed(2)}</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-muted-foreground">VIP</span>
                  <span className="font-semibold">R${session.vipSeatPrice.toFixed(2)}</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-muted-foreground">PWD</span>
                  <span className="font-semibold">R${session.pwdSeatPrice.toFixed(2)}</span>
                </div>
              </div>
            </div>

            <Button
              asChild
              className="w-auto mt-2"
              disabled={!session.hasFreeSeats}
            >
              <Link
                href={`/sessions/${session.id}`}
                tabIndex={session.hasFreeSeats ? 0 : -1}
                aria-disabled={!session.hasFreeSeats}
                className={!session.hasFreeSeats ? "pointer-events-none opacity-50" : ""}
              >
                {session.hasFreeSeats ? 'Select Seats' : 'No Seats Available'}
              </Link>
            </Button>
          </div>
        </div>

      </CardContent>
    </Card>
  );
};

export default SessionCard;