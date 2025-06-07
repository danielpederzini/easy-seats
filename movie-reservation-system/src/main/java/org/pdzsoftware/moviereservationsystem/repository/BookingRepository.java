package org.pdzsoftware.moviereservationsystem.repository;

import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
                select new org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse(
                    b.id,
                    b.bookingStatus,
                    b.totalPrice,
                    b.checkoutId,
                    b.checkoutUrl,
                    b.createdAt,
                    b.updatedAt,
                    b.expiresAt,
                    b.paymentIntentId
                )
                FROM User u
                JOIN u.bookings b
                JOIN b.session s
                WHERE u.id = :userId
                AND b.bookingStatus IN :statuses
                ORDER BY
                    CASE
                        WHEN b.bookingStatus = 'AWAITING_PAYMENT' 
                            OR b.bookingStatus = 'PAYMENT_RETRY' THEN 1
                        WHEN b.bookingStatus = 'PAYMENT_CONFIRMED' THEN 2
                        WHEN b.bookingStatus = 'AWAITING_CANCELLATION' THEN 3
                        WHEN b.bookingStatus = 'CANCELLED' THEN 4
                        WHEN b.bookingStatus = 'PAST' THEN 5
                        ELSE 6
                    END ASC,
                    CASE
                        WHEN b.bookingStatus = 'AWAITING_PAYMENT' 
                            OR b.bookingStatus = 'PAYMENT_RETRY' THEN b.expiresAt
                        WHEN b.bookingStatus = 'PAYMENT_CONFIRMED' THEN s.startTime
                        ELSE NULL
                    END ASC,
                    CASE
                        WHEN b.bookingStatus != 'AWAITING_PAYMENT' 
                            AND b.bookingStatus != 'PAYMENT_RETRY' 
                            AND b.bookingStatus != 'PAYMENT_CONFIRMED' THEN b.updatedAt
                        ELSE NULL
                    END DESC
            """)
    Page<BookingDetailedResponse> findDetailedByFilters(@Param("userId") Long userId,
                                                        @Param("statuses") List<BookingStatus> statuses,
                                                        Pageable pageable);

    @Query("""
                SELECT b
                FROM Booking b
                WHERE b.bookingStatus = 'EXPIRED'
            """)
    List<Booking> findExpiredBookings();

    @Query("""
                SELECT b.id
                FROM Booking b
                WHERE b.bookingStatus = 'AWAITING_DELETION'
                AND b.updatedAt <= :threshold
            """)
    Set<Long> findIdsToDelete(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("""
                UPDATE Booking b
                SET b.bookingStatus = PAST, b.updatedAt = :now
                WHERE b.bookingStatus = PAYMENT_CONFIRMED
                AND b.session.endTime <= :now
            """)
    int markBookingsAsPastIfSessionEnded(@Param("now") LocalDateTime now);

    @Query("""
                SELECT b
                FROM Booking b
                WHERE b.id = :bookingId
                AND b.user.id = :userId
            """)
    Optional<Booking> findByIdAndUserId(@Param("bookingId") Long bookingId,
                                        @Param("userId") Long userId);
}
