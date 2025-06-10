package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.repository.BookedSeatRepository;
import org.pdzsoftware.moviereservationsystem.repository.SeatRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultSeatService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultSeatServiceTest {
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private BookedSeatRepository bookedSeatRepository;
    @Mock
    private SeatCacheService seatCacheService;
    @InjectMocks
    private DefaultSeatService seatService;

    @Test
    void findByIds_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L);
        Long sessionId = 1L;

        // Act
        seatService.findByIds(seatIds, sessionId);

        // Assert
        verify(seatRepository).findAllByIds(seatIds, sessionId);
    }

    @Test
    void findResponsesBySessionId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long sessionId = 1L;

        // Act
        seatService.findResponsesBySessionId(sessionId);

        // Assert
        verify(seatRepository).findResponsesBySessionId(sessionId);
    }

    @Test
    void findIdsByBookingId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long bookingId = 1L;

        // Act
        seatService.findIdsByBookingId(bookingId);

        // Assert
        verify(seatRepository).findIdsByBookingId(bookingId);
    }

    @Test
    void getTakenSeatIdsForSession_always_returnsUnionOfTakenSeatIdsFromSources() {
        // Arrange
        Set<Long> seatIds = Set.of(1L, 2L, 3L, 4L, 5L);
        Long sessionId = 1L;

        when(bookedSeatRepository.findTakenSeatIdsBySessionId(anySet(), anyLong()))
                .thenReturn(Set.of(1L, 3L));
        when(seatCacheService.findTakenIdsBySessionId(anyLong(), anySet()))
                .thenReturn(Set.of(2L, 4L));

        // Act
        Set<Long> takenSeatIds = seatService.getTakenSeatIdsForSession(seatIds, sessionId);

        // Assert
        assertEquals(Set.of(1L, 2L, 3L, 4L), takenSeatIds);
        verify(bookedSeatRepository).findTakenSeatIdsBySessionId(seatIds, sessionId);
        verify(seatCacheService).findTakenIdsBySessionId(sessionId, seatIds);
    }

    @Test
    void getSeatAvailabilityForSessions_always_returnsMapOfUnitedTakenIdsFromSources() {
        // Arrange
        Set<Long> sessionIds = Set.of(1L, 2L, 3L, 4L, 5L);

        List<Object[]> mockSeatIdsBySessionIds = getMockSeatIdsBySessionIds(sessionIds);
        List<Object[]> mockTakenSeatIdsBySessionIds = getMockTakenSeatIdsBySessionIds(sessionIds);
        Map<Long, Set<Long>> mockCacheTakenSeatIdsBySessionIdsMap = getMockCacheTakenSeatIdsBySessionIdsMap();

        when(seatRepository.findIdsBySessionIds(anySet())).thenReturn(
                mockSeatIdsBySessionIds);
        when(bookedSeatRepository.findTakenSeatIdsBySessionIds(anySet(), anySet())).thenReturn(
                mockTakenSeatIdsBySessionIds);
        when(seatCacheService.findTakenIdsBySessionIds(anyMap())).thenReturn(
                mockCacheTakenSeatIdsBySessionIdsMap);

        // Act
        Map<Long, Boolean> seatAvailabilityMap = seatService.getSeatAvailabilityForSessions(sessionIds);

        // Assert
        seatAvailabilityMap.forEach((sessionId, taken) -> {
            if (Set.of(1L, 5L).contains(sessionId)) {
                assertEquals(false, taken);
            } else {
                assertEquals(true, taken);
            }
        });

        verify(seatRepository).findIdsBySessionIds(sessionIds);
    }

    @Test
    void existsByIdAndSessionId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long seatId = 1L;
        Long sessionId = 1L;

        // Act
        seatService.existsByIdAndSessionId(seatId, sessionId);

        // Assert
        verify(seatRepository).existsByIdAndSessionId(seatId, sessionId);
    }

    @Test
    void areAllAvailableToBook_withSeatsAvailable_returnsTrue() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 1L;
        Set<Long> seatIds = Set.of(1L, 2L, 3L);

        when(bookedSeatRepository.isAnyBooked(anyLong(), anySet())).thenReturn(false);
        when(seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId)).thenReturn(false);

        // Act
        boolean allAvailable = seatService.areAllAvailableToBook(sessionId, userId, seatIds);

        // Assert
        assertTrue(allAvailable);
        verify(bookedSeatRepository).isAnyBooked(sessionId, seatIds);
        verify(seatCacheService).isAnyCachedByAnotherUser(seatIds, sessionId, userId);
    }

    @Test
    void areAllAvailableToBook_withSeatsTakenInDatabase_returnsFalse() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 1L;
        Set<Long> seatIds = Set.of(1L, 2L, 3L);

        when(bookedSeatRepository.isAnyBooked(anyLong(), anySet())).thenReturn(true);

        // Act
        boolean allAvailable = seatService.areAllAvailableToBook(sessionId, userId, seatIds);

        // Assert
        assertFalse(allAvailable);
        verify(bookedSeatRepository).isAnyBooked(sessionId, seatIds);
    }

    @Test
    void areAllAvailableToBook_withSeatsTakenInCache_returnsFalse() {
        // Arrange
        Long sessionId = 1L;
        Long userId = 1L;
        Set<Long> seatIds = Set.of(1L, 2L, 3L);

        when(bookedSeatRepository.isAnyBooked(anyLong(), anySet())).thenReturn(false);
        when(seatCacheService.isAnyCachedByAnotherUser(seatIds, sessionId, userId)).thenReturn(true);

        // Act
        boolean allAvailable = seatService.areAllAvailableToBook(sessionId, userId, seatIds);

        // Assert
        assertFalse(allAvailable);
        verify(bookedSeatRepository).isAnyBooked(sessionId, seatIds);
        verify(seatCacheService).isAnyCachedByAnotherUser(seatIds, sessionId, userId);
    }

    @Test
    void reserveInCache_withSeatNotBooked_callsCacheServiceWithoutAlteringArguments() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;
        Long seatId = 1L;

        when(bookedSeatRepository.isAnyBooked(anyLong(), anySet())).thenReturn(false);

        // Act
        seatService.reserveInCache(userId, sessionId, seatId);

        // Assert
        verify(bookedSeatRepository).isAnyBooked(sessionId, Set.of(seatId));
        verify(seatCacheService).reserve(userId, sessionId, seatId);
    }

    @Test
    void reserveInCache_withSeatBooked_throwsConflictException() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;
        Long seatId = 1L;

        when(bookedSeatRepository.isAnyBooked(anyLong(), anySet())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> seatService.reserveInCache(userId, sessionId, seatId))
                .isInstanceOf(ConflictException.class);

        verify(bookedSeatRepository).isAnyBooked(sessionId, Set.of(seatId));
    }

    @Test
    void releaseFromCache_always_callsCacheServiceWithoutAlteringArguments() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;
        Long seatId = 1L;

        // Act
        seatService.releaseFromCache(userId, sessionId, seatId);

        // Assert
        verify(seatCacheService).release(userId, sessionId, seatId);
    }

    @Test
    void clearUserCacheLockForSession_always_callsCacheServiceWithoutAlteringArguments() {
        // Arrange
        Long userId = 1L;
        Long sessionId = 1L;

        // Act
        seatService.clearUserCacheLockForSession(userId, sessionId);

        // Assert
        verify(seatCacheService).clearUserLockForSession(userId, sessionId);
    }

    private List<Object[]> getMockSeatIdsBySessionIds(Set<Long> sessionIds) {
        List<Object[]> result = new ArrayList<>();

        sessionIds.forEach(id -> {
            for (long i = 1; i <= 6; i++) {
                result.add(new Object[]{id, i});
            }
        });

        return result;
    }

    private List<Object[]> getMockTakenSeatIdsBySessionIds(Set<Long> sessionIds) {
        List<Object[]> result = new ArrayList<>();

        sessionIds.forEach(id -> {
            if (id.equals(5L)) {
                for (long i = 1; i <= 6; i++) {
                    result.add(new Object[]{id, i});
                }
            } else {
                for (long i = 1; i <= 5; i = i + 2) {
                    result.add(new Object[]{id, i});
                }
            }
        });

        return result;
    }

    private Map<Long, Set<Long>> getMockCacheTakenSeatIdsBySessionIdsMap() {
        return Map.of(
                1L, Set.of(2L, 4L, 6L),
                2L, Set.of(2L, 4L),
                3L, Set.of(4L, 6L),
                4L, Set.of(4L, 6L),
                5L, Set.of()
        );
    }
}
