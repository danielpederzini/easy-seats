package org.pdzsoftware.moviereservationsystem.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public interface SeatCacheService {
    void reserve(Long seatId, Long sessionId, Long userId);
    void release(Long seatId, Long sessionId, Long userId);
    Set<Long> clearUserLockForSession(Long userId, Long sessionId);
    boolean isAnyCachedByAnotherUser(Set<Long> seatIds, Long sessionId, Long userId);
    Set<Long> findTakenIdsBySessionId(Long sessionId, Set<Long> seatIds);
    Map<Long, Set<Long>> findTakenIdsBySessionIds(Map<Long, Set<Long>> seatIdsBySessionId);
}
