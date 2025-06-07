package org.pdzsoftware.moviereservationsystem.repository;

import org.pdzsoftware.moviereservationsystem.dto.response.SeatResponse;
import org.pdzsoftware.moviereservationsystem.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("""
                SELECT COUNT(s) > 0
                FROM Seat s
                WHERE s.id = :seatId
                AND s.screen.id = (
                    SELECT se.screen.id
                    FROM Session se
                    WHERE se.id = :sessionId
                )
            """)
    boolean existsByIdAndSessionId(
            @Param("seatId") Long seatId,
            @Param("sessionId") Long sessionId
    );

    @Query("""
                SELECT s 
                FROM Seat s
                WHERE s.id IN :seatIds
                AND s.screen.id = (
                    SELECT se.screen.id
                    FROM Session se
                    WHERE se.id = :sessionId
                )
            """)
    List<Seat> findAllByIds(
            @Param("seatIds") Set<Long> seatIds,
            @Param("sessionId") Long sessionId
    );

    @Query("""
                SELECT s.id 
                FROM Seat s
                WHERE s.screen.id = (
                    SELECT se.screen.id
                    FROM Session se
                    WHERE se.id = :sessionId
                )
            """)
    Set<Long> findIdsBySessionId(
            @Param("sessionId") Long sessionId
    );

    @Query("""
                SELECT s.id AS sessionId, st.id AS seatId
                FROM Session s
                JOIN s.screen sc
                JOIN sc.seats st
                WHERE s.id IN :sessionIds
            """)
    List<Object[]> findIdsBySessionIds(@Param("sessionIds") Set<Long> sessionIds);


    @Query("""
                SELECT s.id
                FROM Booking b
                JOIN b.bookedSeats bs
                JOIN bs.seat s
                WHERE b.id = :bookingId
            """)
    Set<Long> findIdsByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
                select new org.pdzsoftware.moviereservationsystem.dto.response.SeatResponse(
                    st.id,
                    st.seatRow,
                    st.seatNumber,
                    st.seatType
                )
                FROM Session s
                JOIN s.screen sc
                JOIN sc.seats st
                WHERE s.id = :sessionId
            """)
    List<SeatResponse> findResponsesBySessionId(
            @Param("sessionId") Long sessionId
    );
}
