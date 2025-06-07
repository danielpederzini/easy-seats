"use client";

import React from "react";
import Link from "next/link";
import { Calendar, Clock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Movie, MovieGenre } from "@/lib/types";

interface MovieCardProps {
  movie: Movie;
}

const MovieCard: React.FC<MovieCardProps> = ({ movie }) => {
  return (
    <Card className="overflow-hidden transition-all duration-300 hover:shadow-lg h-full flex flex-col">
      <div className="aspect-[2/3] relative overflow-hidden">
        <img
          src={movie.posterUrl || "https://images.pexels.com/photos/33129/popcorn-movie-party-entertainment.jpg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"}
          alt={movie.title}
          className="object-cover w-full h-full transition-transform duration-300 hover:scale-105"
        />
        <Badge
          className={`absolute top-2 right-2 bg-black text-white`}
        >
          {movie.genre}
        </Badge>
      </div>
      <CardHeader className="p-4 pb-2">
        <h3 className="font-bold text-lg line-clamp-1">{movie.title}</h3>
      </CardHeader>
      <CardContent className="p-4 pt-0 flex-1 flex flex-col justify-between">
        <p className="text-sm text-muted-foreground line-clamp-3 mb-3">
          {movie.description}
        </p>
        <div className="flex items-center gap-4 text-sm text-muted-foreground mt-auto">
          <div className="flex items-center">
            <Clock className="h-4 w-4 mr-1" />
            {movie.formattedDuration}
          </div>
          <div className="flex items-center">
            <Calendar className="h-4 w-4 mr-1" />
            {new Date(movie.releaseDate).getFullYear()}
          </div>
        </div>
      </CardContent>
      <CardFooter className="p-4 pt-0">
        <Button
          asChild
          className="w-full"
          disabled={!movie.hasSessions}
        >
          <Link
            href={`/movies/${movie.id}/sessions`}
            tabIndex={movie.hasSessions ? 0 : -1}
            aria-disabled={!movie.hasSessions}
            className={!movie.hasSessions ? "pointer-events-none opacity-50" : ""}
          >
            {movie.hasSessions ? "View Sessions" : "No Sessions Available"}
          </Link>
        </Button>
      </CardFooter>
    </Card>
  );
};

export default MovieCard;