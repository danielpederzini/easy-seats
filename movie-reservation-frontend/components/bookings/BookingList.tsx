"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import { Loader2 } from "lucide-react";
import { ApiError } from "next/dist/server/api-utils";
import { getBookings, createQrCode } from "@/lib/api/bookings";
import { BookingDetailed, BookingStatus } from "@/lib/types";
import { useToast } from "@/hooks/use-toast";
import { useAuth } from "@/context/auth-context";
import BookingCard from "./BookingCard";
import { Button } from "../ui/button";

interface BookingListProps {
    filters: {
        status: string;
    };
    cancelBooking: (bookingId: number) => void;
    createAndShowQrCode: (bookingId: number) => void;
    backToMovies: () => void;
}

const BookingList: React.FC<BookingListProps> = ({
    filters, cancelBooking, createAndShowQrCode, backToMovies
}) => {
    const { isAuthenticated, isLoading: authLoading } = useAuth();
    const { toast } = useToast();

    const [loading, setLoading] = useState(true);
    const [bookings, setBookings] = useState<BookingDetailed[]>([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isFetchingMore, setIsFetchingMore] = useState(false);
    const loaderRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        fetchInitial();
    }, [filters]);

    const fetchInitial = async () => {
        setLoading(true);
        setBookings([])
        setCurrentPage(0);
        setHasMore(true);
        fetchBookings(0);
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
        await fetchBookings(currentPage + 1);
    }, [currentPage, isFetchingMore, loading, hasMore, filters]);



    const fetchBookings = async (nextPage: number) => {
        try {
            const moviesPage = await getBookings(nextPage, filters.status);

            if (nextPage === 0) {
                setBookings(moviesPage.content)
            } else {
                setBookings((prev) => [...prev, ...moviesPage.content]);
            }

            setCurrentPage(nextPage);
            setHasMore(!moviesPage.last);
        } catch (err: ApiError | any) {
            const status = err.statusCode;
            var description = "Something went wrong. Please try again later.";

            if (status === 401 || status === 403) {
                description = "You have to login to view your bookings.";
            } else if (status === 500) {
                description = "Something went wrong on the server. Please try again later.";
            } else if (status) {
                description = `Failed with status ${status}.`;
            }

            toast({
                title: "Error fetching bookings",
                description: description,
                variant: "destructive",
                bgColor: "bg-red-500"
            });
        } finally {
            setLoading(false);
            setIsFetchingMore(false);
        }
    };

    const tryCancelBooking = async (bookingId: number) => {
        if (isFetchingMore || loading) return;
        cancelBooking(bookingId);
    }

    const tryCreateQrCode = async (bookingId: number) => {
        if (isFetchingMore || loading) return;
        createAndShowQrCode(bookingId);
    }

    if (authLoading || ((isFetchingMore || loading) && bookings.length === 0 && isAuthenticated)) {
        return (
            <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
        );
    }

    return (
        <>
            {bookings?.length > 0 ? (
                <div className="grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-6 items-start">
                    {bookings.map((booking) => (
                        <BookingCard key={booking.id} booking={booking} cancelBooking={tryCancelBooking} createQrCode={tryCreateQrCode} />
                    ))}
                </div>
            ) : (
                <div className="text-center py-12 rounded-lg">
                    <p className="text-muted-foreground mb-4">
                        {(() => {
                            switch (filters.status) {
                                case BookingStatus.AWAITING_PAYMENT:
                                    return "You have no pending bookings";
                                case BookingStatus.PAYMENT_CONFIRMED:
                                    return "You have no confirmed bookings";
                                case BookingStatus.CANCELLED:
                                    return "You have no cancelled bookings";
                                case BookingStatus.PAST:
                                    return "You have no past bookings";
                                default:
                                    return "You have no bookings yet";
                            }
                        })()}
                    </p>
                    <Button onClick={() => backToMovies()}>
                        Browse Movies
                    </Button>
                </div>
            )}
        </>
    );
}

export default BookingList;