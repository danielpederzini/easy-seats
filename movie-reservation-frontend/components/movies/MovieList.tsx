"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import { Loader2 } from "lucide-react";
import MovieCard from "@/components/movies/MovieCard";
import { getMovies } from "@/lib/api/movies";
import { Movie } from "@/lib/types";
import { ApiError } from "next/dist/server/api-utils";
import { toast } from "@/hooks/use-toast";

interface MovieListProps {
    filters: {
        search: string;
        genre: string;
    };
}

const MovieList: React.FC<MovieListProps> = ({
    filters
}) => {
    const [loading, setLoading] = useState(true);
    const [movies, setMovies] = useState<Movie[]>([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isFetchingMore, setIsFetchingMore] = useState(false);
    const lastSearchRef = useRef<string>(filters.search);
    const loaderRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        if (filters.search != lastSearchRef.current) {
            const handler = setTimeout(() => {
                fetchInitial();
            }, 400);

            return () => clearTimeout(handler);
        } else {
            fetchInitial();
        }
    }, [filters]);

    const fetchInitial = async () => {
        setLoading(true);
        setMovies([]);
        setCurrentPage(0);
        setHasMore(true);
        fetchMovies(0);
    }

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
        await fetchMovies(currentPage + 1);
    }, [currentPage, isFetchingMore, loading, hasMore, filters]);



    const fetchMovies = async (nextPage: number) => {
        try {
            const moviesPage = await getMovies(nextPage, filters.search, filters.genre);

            if (nextPage === 0) {
                setMovies(moviesPage.content)
            } else {
                setMovies((prev) => [...prev, ...moviesPage.content]);
            }

            setCurrentPage(nextPage);
            setHasMore(!moviesPage.last);
        } catch (err: ApiError | any) {
            const status = err.statusCode;
            var description = "Something went wrong. Please try again later.";

            if (status === 500) {
                description = "Something went wrong on the server. Please try again later.";
            } else if (status) {
                description = `Failed with status ${status}. Please try again later.`;
            }

            toast({
                title: "Error fetching more movies",
                description: description,
                variant: "destructive",
                bgColor: "bg-red-500"
            });
        } finally {
            setLoading(false);
            setIsFetchingMore(false);
        }
    };


    if (loading && movies.length === 0) {
        return (
            <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
        );
    }

    return (
        <>
            {movies.length > 0 ? (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
                    {movies.map((movie) => (
                        <MovieCard key={movie.id} movie={movie} />
                    ))}
                </div>
            ) : (
                <div className="text-center py-12">
                    <p className="text-muted-foreground">No movies found matching your criteria.</p>
                </div>
            )}
            {/* Infinite loader sentinel */}
            {hasMore && !loading && movies.length > 0 && (
                <div ref={loaderRef} className="flex justify-center py-8">
                    <Loader2 className="h-6 w-6 animate-spin text-primary" />
                </div>
            )}
        </>
    );
}

export default MovieList;