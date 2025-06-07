package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.pdzsoftware.moviereservationsystem.model.Session;
import org.pdzsoftware.moviereservationsystem.repository.SessionRepository;
import org.pdzsoftware.moviereservationsystem.service.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSessionService implements SessionService {
    private static final Duration TOLERATED_PERIOD = Duration.ofMinutes(20L);

    private final SessionRepository sessionRepository;

    @Override
    public Optional<Session> findById(Long sessionId) {
        return sessionRepository.findById(sessionId);
    }

    @Override
    public Optional<SessionResponse> findResponseByBookingId(Long bookingId) {
        return sessionRepository.findResponseByBookingId(bookingId);
    }

    @Override
    public Optional<Long> findIdByBookingId(Long bookingId) {
        return sessionRepository.findIdByBookingId(bookingId);
    }

    @Override
    public Optional<SessionDetailedResponse> findDetailedResponseById(Long sessionId) {
        return sessionRepository.findDetailedResponseById(sessionId);
    }

    @Override
    public Page<SessionResponse> findResponsesByFilters(Long movieId,
                                                        LocalDate sessionDate,
                                                        Long theaterId,
                                                        int page) {
        LocalDateTime threshold = LocalDateTime.now().minus(TOLERATED_PERIOD);

        LocalDateTime startOfFirstDay = getStartOfFirstDay(sessionDate);
        LocalDateTime endOfLastDay = getEndOfLastDay(sessionDate);

        Pageable pageable = PageRequest.of(page, 15);

        return sessionRepository.findResponsesByFilters(movieId, threshold, startOfFirstDay, endOfLastDay, theaterId, pageable);
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

    @Override
    public boolean existsById(Long sessionId) {
        return sessionRepository.existsById(sessionId);
    }

    @Override
    public boolean isExpired(LocalDateTime startTime) {
        return startTime.isBefore(LocalDateTime.now().minus(TOLERATED_PERIOD));
    }
}
