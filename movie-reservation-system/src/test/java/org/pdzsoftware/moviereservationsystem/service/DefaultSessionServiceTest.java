package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.repository.SessionRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultSessionService;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pdzsoftware.moviereservationsystem.service.impl.DefaultSessionService.TOLERATED_PERIOD;

@ExtendWith(MockitoExtension.class)
class DefaultSessionServiceTest {
    @Mock
    private SessionRepository sessionRepository;
    @InjectMocks
    private DefaultSessionService sessionService;

    @Test
    void findById_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long sessionId = 1L;

        // Act
        sessionService.findById(sessionId);

        // Assert
        verify(sessionRepository).findById(eq(sessionId));
    }

    @Test
    void findResponseByBookingId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long bookingId = 1L;

        // Act
        sessionService.findResponseByBookingId(bookingId);

        // Assert
        verify(sessionRepository).findResponseByBookingId(eq(bookingId));
    }

    @Test
    void findIdByBookingId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long bookingId = 1L;

        // Act
        sessionService.findIdByBookingId(bookingId);

        // Assert
        verify(sessionRepository).findIdByBookingId(eq(bookingId));
    }

    @Test
    void findDetailedResponseById_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long sessionId = 1L;

        // Act
        sessionService.findDetailedResponseById(sessionId);

        // Assert
        verify(sessionRepository).findDetailedResponseById(eq(sessionId));
    }

    @Test
    void findResponsesByFilters_withSessionDate_callsRepositoryWithCorrectArguments() {
        // Arrange
        Long movieId = 1L;
        LocalDate sessionDate = LocalDate.now();
        Long theaterId = 1L;
        int page = 0;

        // Act
        sessionService.findResponsesByFilters(movieId, sessionDate, theaterId, page);

        // Assert
        verify(sessionRepository).findResponsesByFilters(
                eq(movieId),
                any(LocalDateTime.class),
                eq(getStartOfFirstDay(sessionDate)),
                eq(getEndOfLastDay(sessionDate)),
                eq(theaterId),
                any(Pageable.class));
    }

    @Test
    void findResponsesByFilters_withoutSessionDate_callsRepositoryWithCorrectArguments() {
        // Arrange
        Long movieId = 1L;
        Long theaterId = 1L;
        int page = 0;

        // Act
        sessionService.findResponsesByFilters(movieId, null, theaterId, page);

        // Assert
        verify(sessionRepository).findResponsesByFilters(
                eq(movieId),
                any(LocalDateTime.class),
                eq(getStartOfFirstDay(null)),
                eq(getEndOfLastDay(null)),
                eq(theaterId),
                any(Pageable.class));
    }

    @Test
    void existsById_whenExists_returnsTrue() {
        // Arrange
        Long sessionId = 1L;
        when(sessionRepository.existsById(any())).thenReturn(true);

        // Act
        boolean exists = sessionService.existsById(sessionId);

        // Assert
        assertTrue(exists);
        verify(sessionRepository).existsById(eq(sessionId));
    }

    @Test
    void existsById_whenNotExists_returnsFalse() {
        // Arrange
        Long sessionId = 1L;
        when(sessionRepository.existsById(any())).thenReturn(false);

        // Act
        boolean exists = sessionService.existsById(sessionId);

        // Assert
        assertFalse(exists);
        verify(sessionRepository).existsById(eq(sessionId));
    }

    @Test
    void isExpired_whenIsExpired_returnsTrue() {
        // Act
        boolean expired = sessionService.isExpired(LocalDateTime.now().minusDays(1L));

        // Assert
        assertTrue(expired);
    }

    @Test
    void isExpired_whenIsNotExpired_returnsFalse() {
        // Act
        boolean expired = sessionService.isExpired(LocalDateTime.now().minus(TOLERATED_PERIOD));

        // Assert
        assertFalse(expired);
    }

    private static LocalDateTime getEndOfLastDay(LocalDate sessionDate) {
        return sessionDate != null ?
                sessionDate.plusDays(2).atStartOfDay().minusSeconds(1) :
                LocalDate.now().plusDays(366).atStartOfDay().minusSeconds(1);
    }

    private static LocalDateTime getStartOfFirstDay(LocalDate sessionDate) {
        return sessionDate != null ?
                sessionDate.atStartOfDay() :
                LocalDate.now().atStartOfDay();
    }
}
