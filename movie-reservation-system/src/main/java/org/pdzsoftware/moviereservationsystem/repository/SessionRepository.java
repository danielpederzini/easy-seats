package org.pdzsoftware.moviereservationsystem.repository;

import org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.pdzsoftware.moviereservationsystem.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    @Query("""
                SELECT new org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse(
                    s.id,
                    s.startTime,
                    s.endTime,
                    s.audioLanguage,
                    s.hasSubtitles,
                    s.isThreeD,
                    s.standardSeatPrice,
                    s.vipSeatPrice,
                    s.pwdSeatPrice,
                    t.id,
                    t.theaterName,
                    t.logoUrl,
                    t.fullAddressLine,
                    sc.screenName
                )
                FROM Session s
                JOIN s.screen sc
                JOIN sc.theater t
                WHERE s.movie.id = :id
                AND s.startTime >= :threshold
                AND s.startTime >= :startOfFirstDay
                AND s.startTime < :endOfLastDay
                AND (:theaterId IS NULL OR t.id = :theaterId)
                ORDER BY s.startTime ASC
            """)
    Page<SessionResponse> findResponsesByFilters(@Param("id") Long id,
                                                 @Param("threshold") LocalDateTime threshold,
                                                 @Param("startOfFirstDay") LocalDateTime startOfFirstDay,
                                                 @Param("endOfLastDay") LocalDateTime endOfLastDay,
                                                 @Param("theaterId") Long theaterId,
                                                 Pageable pageable);

    @Query("""
                select new org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse(
                    s.id,
                    s.startTime,
                    s.endTime,
                    s.audioLanguage,
                    s.hasSubtitles,
                    s.isThreeD,
                    s.standardSeatPrice,
                    s.vipSeatPrice,
                    s.pwdSeatPrice,
                    t.id,
                    t.theaterName,
                    t.logoUrl,
                    t.fullAddressLine,
                    sc.screenName
                )
                FROM Booking b
                JOIN b.session s
                JOIN s.screen sc
                JOIN sc.theater t
                WHERE b.id = :bookingId
            """)
    Optional<SessionResponse> findResponseByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
                SELECT s.id
                FROM Booking b
                JOIN b.session s
                WHERE b.id = :bookingId
            """)
    Optional<Long> findIdByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
                select new org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse(
                    s.id,
                    s.startTime,
                    s.endTime,
                    s.audioLanguage,
                    s.hasSubtitles,
                    s.isThreeD,
                    s.standardSeatPrice,
                    s.vipSeatPrice,
                    s.pwdSeatPrice,
                    t.id,
                    t.theaterName,
                    t.logoUrl,
                    t.fullAddressLine,
                    sc.screenName
                )
                FROM Session s
                JOIN s.screen sc
                JOIN sc.theater t
                WHERE s.id = :id
            """)
    Optional<SessionDetailedResponse> findDetailedResponseById(@Param("id") Long id);
}
