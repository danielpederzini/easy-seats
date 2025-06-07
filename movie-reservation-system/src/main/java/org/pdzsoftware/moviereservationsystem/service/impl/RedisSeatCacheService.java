package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.service.SeatCacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSeatCacheService implements SeatCacheService {
    private static final int LOCK_TTL_MS = 5 * 60 * 1000;

    private static final String USER_LOCKS_KEY_PREFIX = "UserLocks:";

    private static final String KEY_PREFIX = "Seat:";
    private static final String VALUE_PREFIX = "UserID:";

    private final RedisTemplate<String, String> template;

    @Override
    public void reserve(Long seatId, Long sessionId, Long userId) {
        try {
            String seatKey = buildSeatKey(seatId, sessionId);

            if (template.hasKey(seatKey)) {
                throw new ConflictException("Seat is already temporarily reserved");
            }

            template.opsForValue().set(seatKey, VALUE_PREFIX + userId, LOCK_TTL_MS, TimeUnit.MILLISECONDS);

            String userLocksKey = buildUserLocksKey(userId);
            template.opsForSet().add(userLocksKey, seatKey);
        } catch (Exception ex) {
            log.error("[RedisSeatCacheService] Error reserving seat in Redis", ex);
            throw new InternalErrorException("Internal error reserving seat in cache");
        }
    }

    @Override
    public void release(Long seatId, Long sessionId, Long userId) {
        try {
            String key = buildSeatKey(seatId, sessionId);
            String value = template.opsForValue().get(key);

            if (value == null) {
                return;
            }

            if (!value.equals(VALUE_PREFIX + userId)) {
                throw new ConflictException("Seat is temporarily reserved by another user");
            }

            template.delete(key);

            String userLocksKey = buildUserLocksKey(userId);
            template.opsForSet().remove(userLocksKey, key);
        } catch (Exception ex) {
            if (ex instanceof ConflictException) throw ex;
            log.error("[RedisSeatCacheService] Error releasing seat in Redis", ex);
            throw new InternalErrorException("Internal error releasing seat in cache");
        }
    }

    @Override
    public Set<Long> clearUserLockForSession(Long userId, Long sessionId) {
        try {
            String userLocksKey = buildUserLocksKey(userId);
            Set<String> keysToRemove = template.opsForSet().members(userLocksKey);

            if (keysToRemove == null) {
                return Collections.emptySet();
            }

            keysToRemove.removeIf(key -> !key.endsWith(":" + sessionId));

            if (!keysToRemove.isEmpty()) {
                template.opsForSet().remove(userLocksKey, keysToRemove.toArray());
                template.delete(keysToRemove);
            }

            return keysToRemove.stream()
                    .map(key -> Long.parseLong(key.split(":")[1]))
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            log.error("[RedisSeatCacheService] Error clearing user seat lock for session in Redis", ex);
            throw new InternalErrorException("Internal error clearing user seat lock for session in cache");
        }
    }

    @Override
    public boolean isAnyCachedByAnotherUser(Set<Long> seatIds, Long sessionId, Long userId) {
        try {
            List<String> keys = buildSeatKeys(seatIds, sessionId);
            List<String> values = template.opsForValue().multiGet(keys);

            if (values == null || values.isEmpty()) {
                return false;
            }

            return !doAllValuesMatch(values, VALUE_PREFIX + userId);
        } catch (Exception ex) {
            log.error("[RedisSeatCacheService] Error validating seats in Redis", ex);
            throw new InternalErrorException("Internal error validating booked seats in cache");
        }
    }

    @Override
    public Set<Long> findTakenIdsBySessionId(Long sessionId, Set<Long> seatIds) {
        try {
            Set<Long> takenSeatIds = new HashSet<>(seatIds);
            takenSeatIds.removeIf(seatId -> !template.hasKey(buildSeatKey(seatId, sessionId)));
            return takenSeatIds;
        } catch (Exception ex) {
            log.error("[RedisSeatCacheService] Error finding taken seat IDs in Redis", ex);
            throw new InternalErrorException("Internal error finding taken seat IDs in cache");
        }
    }

    @Override
    public Map<Long, Set<Long>> findTakenIdsBySessionIds(Map<Long, Set<Long>> seatIdsBySessionId) {
        Map<Long, Set<Long>> takenIdsBySessionId = new HashMap<>();

        seatIdsBySessionId.forEach((sessionId, seatIds) -> {
            Set<Long> takenSeatIds = findTakenIdsBySessionId(sessionId, seatIds);
            takenIdsBySessionId.put(sessionId, takenSeatIds);
        });

        return takenIdsBySessionId;
    }


    private boolean doAllValuesMatch(List<String> values, String targetValue) {
        return values.stream().filter(Objects::nonNull).allMatch(value -> value.equals(targetValue));
    }

    private static String buildUserLocksKey(Long userId) {
        return USER_LOCKS_KEY_PREFIX + userId;
    }

    private static String buildSeatKey(Long seatId, Long sessionId) {
        return KEY_PREFIX + seatId + ":" + sessionId;
    }

    private static List<String> buildSeatKeys(Set<Long> seatIds, Long sessionId) {
        return seatIds.stream().map(id -> KEY_PREFIX + id + ":" + sessionId).toList();
    }
}

