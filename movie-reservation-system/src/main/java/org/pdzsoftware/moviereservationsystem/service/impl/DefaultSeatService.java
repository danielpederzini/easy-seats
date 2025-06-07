package org.pdzsoftware.moviereservationsystem.service.impl;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.SeatResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.model.Seat;
import org.pdzsoftware.moviereservationsystem.repository.BookedSeatRepository;
import org.pdzsoftware.moviereservationsystem.repository.SeatRepository;
import org.pdzsoftware.moviereservationsystem.service.SeatCacheService;
import org.pdzsoftware.moviereservationsystem.service.SeatService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSeatService implements SeatService {
    private final SeatRepository seatRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final SeatCacheService seatCacheService;

    @Override
    public List<Seat> findByIds(Set<Long> seatIds, Long sessionId) {
        return seatRepository.findAllByIds(seatIds, sessionId);
    }

    @Override
    public List<SeatResponse> findResponsesBySessionId(Long sessionId) {
        return seatRepository.findResponsesBySessionId(sessionId);
    }

    @Override
    public Set<Long> findIdsByBookingId(Long bookingId) {
        return seatRepository.findIdsByBookingId(bookingId);
    }

    @Override
    public Set<Long> getTakenSeatIdsForSession(Set<Long> seatIds, Long sessionId) {
        Set<Long> takenSeatIds = this.findTakenIdsBySessionId(sessionId, seatIds);
        Set<Long> takenSeatIdsCache = seatCacheService.findTakenIdsBySessionId(sessionId, seatIds);

        return Sets.union(takenSeatIds, takenSeatIdsCache);
    }

    @Override
    public Map<Long, Boolean> getSeatAvailabilityForSessions(Set<Long> sessionIds) {
        Map<Long, Set<Long>> seatIdsBySessionId = this.findIdsBySessionIds(sessionIds);
        Map<Long, Set<Long>> takenSeatIdsBySessionId = this.findTakenIdsBySessionIds(sessionIds, seatIdsBySessionId);
        Map<Long, Set<Long>> takenSeatIdsBySessionIdCache = seatCacheService.findTakenIdsBySessionIds(seatIdsBySessionId);

        Map<Long, Boolean> sessionHasFreeSeats = new HashMap<>();

        sessionIds.forEach(sessionId -> {
            Set<Long> allTakenSeats = Sets.union(
                    takenSeatIdsBySessionId.getOrDefault(sessionId, Collections.emptySet()),
                    takenSeatIdsBySessionIdCache.getOrDefault(sessionId, Collections.emptySet())
            );
            Set<Long> allSeats = seatIdsBySessionId.getOrDefault(sessionId, Collections.emptySet());
            sessionHasFreeSeats.put(sessionId, allTakenSeats.size() < allSeats.size());
        });

        return sessionHasFreeSeats;
    }

    private Map<Long, Set<Long>> findIdsBySessionIds(Set<Long> sessionIds) {
        Map<Long, Set<Long>> seatIdsBySessionId = new HashMap<>();

        for (Object[] result : seatRepository.findIdsBySessionIds(sessionIds)) {
            Long sessionId = (Long) result[0];
            Long seatId = (Long) result[1];
            seatIdsBySessionId.computeIfAbsent(sessionId, k -> new HashSet<>()).add(seatId);
        }

        return seatIdsBySessionId;
    }

    private Set<Long> findTakenIdsBySessionId(Long sessionId, Set<Long> seatIds) {
        return bookedSeatRepository.findTakenSeatIdsBySessionId(seatIds, sessionId);
    }

    private Map<Long, Set<Long>> findTakenIdsBySessionIds(Set<Long> sessionIds,
                                                          Map<Long, Set<Long>> seatIdsBySessionId) {
        Set<Long> allSeatIds = seatIdsBySessionId.values().stream().flatMap(Set::stream).collect(Collectors.toSet());

        Map<Long, Set<Long>> takenSeatIdsBySessionId = new HashMap<>();

        for (Object[] row : bookedSeatRepository.findTakenSeatIdsBySessionIds(allSeatIds, sessionIds)) {
            Long sessionId = (Long) row[0];
            Long seatId = (Long) row[1];
            takenSeatIdsBySessionId.computeIfAbsent(sessionId, k -> new HashSet<>()).add(seatId);
        }

        return takenSeatIdsBySessionId;
    }

    @Override
    public boolean existsByIdAndSessionId(Long seatId, Long sessionId) {
        return seatRepository.existsByIdAndSessionId(seatId, sessionId);
    }

    @Override
    public boolean areAllAvailableToBook(Long sessionId, Long userId, Set<Long> seatIds) {
        return !isAnyBookedInDatabase(sessionId, seatIds) && !isAnyCachedByAnotherUser(userId, sessionId, seatIds);
    }

    @Override
    public void reserveInCache(Long userId, Long sessionId, Long seatId) {
        if (isAnyBookedInDatabase(sessionId, Set.of(seatId))) {
            throw new ConflictException("Seat is already booked");
        }

        seatCacheService.reserve(seatId, sessionId, userId);
    }

    @Override
    public void releaseFromCache(Long userId, Long sessionId, Long seatId) {
        seatCacheService.release(seatId, sessionId, userId);
    }

    @Override
    public Set<Long> clearUserCacheLockForSession(Long userId, Long sessionId) {
        return seatCacheService.clearUserLockForSession(userId, sessionId);
    }

    private boolean isAnyBookedInDatabase(Long sessionId, Set<Long> seatIds) {
        return bookedSeatRepository.isAnyBooked(sessionId, seatIds);
    }

    private boolean isAnyCachedByAnotherUser(Long userId, Long sessionId, Set<Long> seatIds) {
        return seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId);
    }
}

