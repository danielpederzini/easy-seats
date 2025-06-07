"use client";

import React, { useEffect, useState } from "react";
import { ChevronLeft, Loader2, Clock, CalendarDays } from "lucide-react";
import { getMovie } from "@/lib/api/movies";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Movie } from "@/lib/types";
import MovieSessionList from "@/components/movies/MovieSessionList";
import { ApiError } from "next/dist/server/api-utils";
import { toast } from "@/hooks/use-toast";

interface MovieSessionsPageProps {
  params: {
    id: number;
  }
}

const MovieSessionsPage: React.FC<MovieSessionsPageProps> = ({
  params
}) => {
  const [loading, setLoading] = useState(true);
  const [movie, setMovie] = useState<Movie | null>(null);

  useEffect(() => {
    setLoading(true);
    setMovie(null);
    fetchMovie();
  }, [params]);

  const fetchMovie = async () => {
    try {
      const movie = await getMovie(params.id);
      setMovie(movie);
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      var description = "Something went wrong. Please try again later.";

      if (status === 500) {
        description = "Something went wrong on the server. Please try again later.";
      } else if (status) {
        description = `Failed with status ${status}.`;
      }

      toast({
        title: "Error fetching movie sessions",
        description: description,
        variant: "destructive",
        bgColor: "bg-red-500"
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading && !movie) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!movie) {
    return (
      <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
        <div className="container py-8 flex items-center justify-center">
          <div className="text-center py-12">
            <p className="text-muted-foreground">Something went wrong</p>
            <Button asChild className="mt-4">
              <Link href="/movies">Back to Movies</Link>
            </Button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="flex items-center justify-center w-full py-3 md:py-6 lg:py-8 xl:py-12 p-8 bg-black relative overflow-hidden">
      <div className="container py-8">
        <Button variant="ghost" asChild className="mb-6">
          <Link href="/movies">
            <ChevronLeft className="mr-2 h-4 w-4" />
            Back to Movies
          </Link>
        </Button>

        <div className="flex flex-row gap-8 mb-8">
          <div className="overflow-hidden rounded-lg max-h-96">
            <img
              src={movie.posterUrl || "https://images.pexels.com/photos/33129/popcorn-movie-party-entertainment.jpg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"}
              alt={movie.title}
              className="object-cover w-full h-full max-h-96"
              style={{ aspectRatio: "2 / 3" }}
            />
          </div>

          <div className="md:col-span-2 space-y-4">
            <h1 className="text-3xl font-bold">{movie.title}</h1>

            <div className="flex items-center gap-4 text-sm text-muted-foreground">
              <div className="flex items-center">
                <Clock className="h-4 w-4 mr-1" />
                {movie.formattedDuration}
              </div>
              <div className="flex items-center">
                <CalendarDays className="h-4 w-4 mr-1" />
                {new Date(movie.releaseDate).getFullYear()}
              </div>
              <div className="px-2 py-1 rounded-full bg-primary/10 text-primary text-xs">
                {movie.genre}
              </div>
            </div>

            <p className="text-muted-foreground">{movie.description}</p>
          </div>
        </div>

        <h2 className="text-2xl font-bold mb-4">Available Sessions</h2>
        <MovieSessionList movie={movie} />
      </div>
    </section>
  );
}

export default MovieSessionsPage;