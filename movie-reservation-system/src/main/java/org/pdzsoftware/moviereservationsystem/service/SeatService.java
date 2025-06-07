package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.response.SeatResponse;
import org.pdzsoftware.moviereservationsystem.model.Seat;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public interface SeatService {
    List<Seat> findByIds(Set<Long> seatIds, Long sessionId);
    List<SeatResponse> findResponsesBySessionId(Long sessionId);
    Set<Long> findIdsByBookingId(Long bookingId);
    Set<Long> getTakenSeatIdsForSession(Set<Long> seatIds, Long sessionId);
    Map<Long, Boolean> getSeatAvailabilityForSessions(Set<Long> sessionIds);

    boolean existsByIdAndSessionId(Long seatId, Long sessionId);
    boolean areAllAvailableToBook(Long sessionId, Long userId, Set<Long> seatIds);

    void reserveInCache(Long userId, Long sessionId, Long seatId);
    void releaseFromCache(Long userId, Long sessionId, Long seatId);
    Set<Long> clearUserCacheLockForSession(Long userId, Long sessionId);
}
