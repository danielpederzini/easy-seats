package org.pdzsoftware.moviereservationsystem.repository;

import org.pdzsoftware.moviereservationsystem.dto.response.BookedSeatResponse;
import org.pdzsoftware.moviereservationsystem.model.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {
    @Query("""
            SELECT CASE 
                     WHEN COUNT(bs) > 0 THEN true 
                     ELSE false 
                   END
            FROM BookedSeat bs 
            WHERE bs.booking.session.id = :sessionId
              AND bs.booking.bookingStatus IN (AWAITING_PAYMENT, PAYMENT_RETRY, PAYMENT_CONFIRMED)
              AND bs.seat.id IN :seatIds
            """)
    boolean isAnyBooked(
            @Param("sessionId") Long sessionId,
            @Param("seatIds") Set<Long> seatIds
    );

    @Query("""
                SELECT st.id
                FROM BookedSeat bs
                JOIN bs.seat st
                JOIN bs.booking b
                JOIN b.session s
                WHERE b.bookingStatus IN (AWAITING_PAYMENT, PAYMENT_RETRY, PAYMENT_CONFIRMED)
                  AND st.id IN :seatIds
                  AND s.id = :sessionId
            """)
    Set<Long> findTakenSeatIdsBySessionId(
            @Param("seatIds") Set<Long> seatIds,
            @Param("sessionId") Long sessionId
    );

    @Query("""
                SELECT s.id AS sessionId, st.id AS seatId
                FROM BookedSeat bs
                JOIN bs.seat st
                JOIN bs.booking b
                JOIN b.session s
                WHERE b.bookingStatus IN (AWAITING_PAYMENT, PAYMENT_RETRY, PAYMENT_CONFIRMED)
                  AND st.id IN :seatIds
                  AND s.id IN :sessionIds
            """)
    List<Object[]> findTakenSeatIdsBySessionIds(
            @Param("seatIds") Set<Long> seatIds,
            @Param("sessionIds") Set<Long> sessionIds
    );


    @Query("""
                select new org.pdzsoftware.moviereservationsystem.dto.response.BookedSeatResponse(
                    bs.id,
                    s.seatRow,
                    s.seatNumber,
                    s.seatType,
                    bs.seatPrice
                )
                FROM BookedSeat bs
                JOIN bs.seat s
                JOIN bs.booking b
                WHERE b.id = :bookingId
            """)
    List<BookedSeatResponse> findResponseByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
                SELECT bs.id
                FROM BookedSeat bs
                JOIN bs.booking b
                ON b.id IN :bookingIds
            """)
    Set<Long> findIdsToDelete(@Param("bookingIds") Set<Long> bookingIds);
}
