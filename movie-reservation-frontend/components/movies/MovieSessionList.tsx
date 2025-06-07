"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import SessionCard from "@/components/sessions/SessionCard";
import { getMovieSessions } from "@/lib/api/movies";
import { Movie, Session } from "@/lib/types";
import { toast } from "@/hooks/use-toast";
import { ApiError } from "next/dist/server/api-utils";
import { Loader2 } from "lucide-react";

interface MovieSessionListProps {
  movie: Movie;
}

const MovieSessionList: React.FC<MovieSessionListProps> = ({
  movie,
}) => {
  const [loading, setLoading] = useState(true);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isFetchingMore, setIsFetchingMore] = useState(false);
  const loaderRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    setLoading(true);
    setSessions([]);
    setCurrentPage(0);
    setHasMore(true);
    fetchMovieSessions(0);
  }, [movie]);

  // Infinite scroll observer
  useEffect(() => {
    if (!hasMore || loading || isFetchingMore) return;
    const observer = new window.IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          fetchMore();
        }
      },
      { threshold: 1 }
    );
    if (loaderRef.current) {
      observer.observe(loaderRef.current);
    }
    return () => {
      if (loaderRef.current) observer.unobserve(loaderRef.current);
    };
  }, [hasMore, loading, isFetchingMore, loaderRef.current]);


  const fetchMore = useCallback(async () => {
    if (isFetchingMore || loading || !hasMore) return;
    setIsFetchingMore(true);
    fetchMovieSessions(currentPage + 1);
  }, [currentPage, isFetchingMore, loading, hasMore]);


  const fetchMovieSessions = async (nextPage: number) => {
    try {
      const movieSessions = await getMovieSessions(movie.id, nextPage);

      if (nextPage === 0) {
        setSessions(movieSessions.content);
      } else {
        setSessions((prev) => [...prev, ...movieSessions.content]);
      }

      setCurrentPage(nextPage);
      setHasMore(!movieSessions.last);
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
      setIsFetchingMore(false);
    }
  };

  if (loading && sessions.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <>
      {sessions.length > 0 ? (
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
          {sessions.map((session) => (
            <SessionCard
              key={session.id}
              session={session}
              movieTitle={movie.title}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-8 bg-muted/30 rounded-lg">
          <p className="text-muted-foreground">No sessions available for the selected filters.</p>
        </div>
      )}
    </>
  );
}

export default MovieSessionList;