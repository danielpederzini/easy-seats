package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.service.impl.RedisSeatCacheService;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisSeatCacheServiceTest {
    private static final String USER_LOCKS_KEY_PREFIX = "UserLocks:";

    private static final String KEY_PREFIX = "Seat:";
    private static final String VALUE_PREFIX = "UserID:";

    @Mock
    private RedisTemplate<String, String> template;
    @InjectMocks
    private RedisSeatCacheService seatCacheService;

    private ValueOperations<String, String> valueOps;
    private SetOperations<String, String> setOps;

    @BeforeEach
    void setUp() {
        valueOps = mock(ValueOperations.class);
        setOps = mock(SetOperations.class);
    }

    @Test
    void reserve_withSeatAvailable_setsKeyAndAddsToUserLocksKey() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(template.opsForSet()).thenReturn(setOps);
        when(template.hasKey(any())).thenReturn(false);
        doNothing().when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(setOps.add(anyString(), anyString())).thenReturn(1L);

        // Act
        seatCacheService.reserve(seatId, sessionId, userId);

        // Assert
        String seatKey = buildSeatKey(seatId, sessionId);
        String userLocksKey = buildUserLocksKey(userId);

        verify(valueOps).set(
                eq(seatKey),
                eq(VALUE_PREFIX + userId),
                anyLong(), any(TimeUnit.class)
        );

        verify(setOps).add(
                eq(userLocksKey),
                eq(seatKey)
        );
    }

    @Test
    void reserve_withSeatUnavailable_throwsConflictException() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.hasKey(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.reserve(seatId, sessionId, userId))
                .isInstanceOf(ConflictException.class);

        String seatKey = buildSeatKey(seatId, sessionId);

        verify(template).hasKey(eq(seatKey));
        verify(valueOps, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(setOps, never()).add(anyString(), anyString());
    }

    @Test
    void reserve_withRedisError_throwsInternalErrorException() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.hasKey(any())).thenThrow(new RedisConnectionFailureException("Connection Failure"));

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.reserve(seatId, sessionId, userId))
                .isInstanceOf(InternalErrorException.class);

        verify(valueOps, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(setOps, never()).add(anyString(), anyString());
    }

    @Test
    void release_withValidKeyValue_deletesKeyAndRemovesFromUserLocksKey() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(template.opsForSet()).thenReturn(setOps);
        when(valueOps.get(anyString())).thenReturn(VALUE_PREFIX + userId);
        when(template.delete(anyString())).thenReturn(true);
        when(setOps.remove(anyString(), anyString())).thenReturn(1L);

        // Act
        seatCacheService.release(seatId, sessionId, userId);

        // Assert
        String seatKey = buildSeatKey(seatId, sessionId);
        String userLocksKey = buildUserLocksKey(userId);

        verify(template).delete(
                eq(seatKey)
        );

        verify(setOps).remove(
                eq(userLocksKey),
                eq(seatKey)
        );
    }

    @Test
    void release_withNullKeyValue_doesNothing() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        // Act
        seatCacheService.release(seatId, sessionId, userId);

        // Assert
        verify(template, never()).delete(anyString());
        verify(setOps, never()).remove(anyString(), anyString());
    }

    @Test
    void release_withConflictingKeyValue_throwsConflictException() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(VALUE_PREFIX + 42L);

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.release(seatId, sessionId, userId))
                .isInstanceOf(ConflictException.class);

        String seatKey = buildSeatKey(seatId, sessionId);

        verify(valueOps).get(eq(seatKey));
        verify(template, never()).delete(anyString());
        verify(setOps, never()).remove(anyString(), anyString());
    }

    @Test
    void release_withRedisError_throwsInternalErrorException() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 2L;
        Long userId = 3L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenThrow(new RedisConnectionFailureException("Connection Failure"));

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.release(seatId, sessionId, userId))
                .isInstanceOf(InternalErrorException.class);

        verify(template, never()).delete(anyString());
        verify(setOps, never()).remove(anyString(), anyString());
    }

    @Test
    void clearUserLockForSession_withMatchingKeys_removesKeysAndReturnsSet() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;
        String userLocksKey = buildUserLocksKey(userId);

        Set<String> keysToRemove = new HashSet<>();
        keysToRemove.add("Seat:1:1");
        keysToRemove.add("Seat:1:2");
        keysToRemove.add("Seat:1:3");

        when(template.opsForSet()).thenReturn(setOps);
        when(setOps.members(anyString())).thenReturn(new HashSet<>(keysToRemove));

        // Act
        Set<Long> result = seatCacheService.clearUserLockForSession(userId, sessionId);

        // Assert
        keysToRemove.removeIf(key -> !key.endsWith(":" + sessionId));
        Set<Long> expected = keysToRemove.stream()
                .map(key -> Long.parseLong(key.split(":")[1]))
                .collect(Collectors.toSet());

        assertEquals(expected, result);
        verify(setOps).members(userLocksKey);
        verify(setOps).remove(eq(userLocksKey), eq(keysToRemove.toArray()));
        verify(template).delete(keysToRemove);
    }

    @Test
    void clearUserLockForSession_withNoKeysInSet_returnsEmptySet() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;
        String userLocksKey = buildUserLocksKey(userId);

        when(template.opsForSet()).thenReturn(setOps);
        when(setOps.members(anyString())).thenReturn(null);

        // Act
        Set<Long> result = seatCacheService.clearUserLockForSession(userId, sessionId);

        // Assert
        assertTrue(result.isEmpty());
        verify(setOps).members(userLocksKey);
        verify(setOps, never()).remove(anyString(), anyIterable());
        verify(template, never()).delete(anyString());
    }

    @Test
    void clearUserLockForSession_withNoMatchingKeys_returnsEmptySet() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;
        String userLocksKey = buildUserLocksKey(userId);

        Set<String> keysToRemove = new HashSet<>();
        keysToRemove.add("Seat:1:2");
        keysToRemove.add("Seat:1:3");

        when(template.opsForSet()).thenReturn(setOps);
        when(setOps.members(userLocksKey)).thenReturn(new HashSet<>(keysToRemove));

        // Act
        Set<Long> result = seatCacheService.clearUserLockForSession(userId, sessionId);

        // Assert
        assertTrue(result.isEmpty());
        verify(setOps).members(userLocksKey);
        verify(setOps, never()).remove(anyString(), anyIterable());
        verify(template, never()).delete(anyString());
    }

    @Test
    void clearUserLockForSession_withRedisError_throwsInternalErrorException() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;

        when(template.opsForSet()).thenReturn(setOps);
        when(setOps.members(anyString())).thenThrow(new RedisConnectionFailureException("Connection Failure"));

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.clearUserLockForSession(userId, sessionId))
                .isInstanceOf(InternalErrorException.class);

        verify(setOps, never()).remove(anyString(), anyIterable());
        verify(template, never()).delete(anyString());
    }

    @Test
    void isAnyCachedByAnotherUser_withAllValuesMatching_returnsFalse() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long userId = 1L;
        Long sessionId = 1L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.multiGet(anyCollection())).thenReturn(List.of(
                VALUE_PREFIX + 1L, VALUE_PREFIX + 1L, VALUE_PREFIX + 1L
        ));

        // Act
        boolean result = seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isAnyCachedByAnotherUser_withNotAllValuesMatching_returnsTrue() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long userId = 1L;
        Long sessionId = 1L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.multiGet(anyCollection())).thenReturn(List.of(
                VALUE_PREFIX + 1L, VALUE_PREFIX + 1L, VALUE_PREFIX + 2L
        ));

        // Act
        boolean result = seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isAnyCachedByAnotherUser_withNullValues_returnsFalse() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long userId = 1L;
        Long sessionId = 1L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.multiGet(anyCollection())).thenReturn(null);

        // Act
        boolean result = seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isAnyCachedByAnotherUser_withEmptyValues_returnsFalse() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long userId = 1L;
        Long sessionId = 1L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.multiGet(anyCollection())).thenReturn(List.of());

        // Act
        boolean result = seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isAnyCachedByAnotherUser_withRedisError_throwsInternalErrorException() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long userId = 1L;
        Long sessionId = 1L;

        when(template.opsForValue()).thenReturn(valueOps);
        when(valueOps.multiGet(anyCollection())).thenThrow(new RedisConnectionFailureException("Connection Failure"));

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId))
                .isInstanceOf(InternalErrorException.class);
    }

    @Test
    void findTakenIdsBySessionId_always_returnsSetOfTakenIdsOnly() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L);
        Long sessionId = 1L;

        String seatKey = buildSeatKey(1L, sessionId);
        String seatKey2 = buildSeatKey(2L, sessionId);

        when(template.hasKey(seatKey)).thenReturn(true);
        when(template.hasKey(seatKey2)).thenReturn(false);

        // Act
        Set<Long> takenSeatIds = seatCacheService.findTakenIdsBySessionId(sessionId, seatIds);

        // Assert
        assertEquals(Set.of(1L), takenSeatIds);
    }

    @Test
    void findTakenIdsBySessionId_withRedisError_throwsInternalErrorException() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long sessionId = 1L;

        when(template.hasKey(anyString())).thenThrow(new RedisConnectionFailureException("Connection Failure"));

        // Act & Assert
        assertThatThrownBy(() -> seatCacheService.findTakenIdsBySessionId(sessionId, seatIds))
                .isInstanceOf(InternalErrorException.class);
    }

    @Test
    void findTakenIdsBySessionIds_always_returnsMapOfIds() {
        // Arrange
        Long sessionId = 1L;
        Long sessionId2 = 2L;

        Map<Long, Set<Long>> seatIdsBySessionIds = Map.of(
                sessionId, Set.of(1L, 2L),
                sessionId2, Set.of(3L, 4L)
        );

        String seatKey = buildSeatKey(1L, sessionId);
        String seatKey2 = buildSeatKey(2L, sessionId);
        String seatKey3 = buildSeatKey(3L, sessionId2);
        String seatKey4 = buildSeatKey(4L, sessionId2);

        when(template.hasKey(seatKey)).thenReturn(true);
        when(template.hasKey(seatKey2)).thenReturn(false);
        when(template.hasKey(seatKey3)).thenReturn(false);
        when(template.hasKey(seatKey4)).thenReturn(true);

        // Act
        Map<Long, Set<Long>> idsMap = seatCacheService.findTakenIdsBySessionIds(seatIdsBySessionIds);

        // Assert
        idsMap.forEach((key, value) -> {
            if (key.equals(sessionId)) {
                assertEquals(Set.of(1L), value);
            } else if (key.equals(sessionId2)) {
                assertEquals(Set.of(4L), value);
            } else {
                fail("Unexpected key: " + key);
            }
        });
    }

    private static String buildUserLocksKey(Long userId) {
        return USER_LOCKS_KEY_PREFIX + userId;
    }

    private static String buildSeatKey(Long seatId, Long sessionId) {
        return KEY_PREFIX + seatId + ":" + sessionId;
    }
}
