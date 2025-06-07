package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.pdzsoftware.moviereservationsystem.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface SessionService {
    Optional<Session> findById(Long sessionId);
    Optional<SessionResponse> findResponseByBookingId(Long bookingId);
    Optional<Long> findIdByBookingId(Long bookingId);
    Optional<SessionDetailedResponse> findDetailedResponseById(Long sessionId);
    Page<SessionResponse> findResponsesByFilters(Long movieId, LocalDate sessionDate, Long theaterId, int page);

    boolean existsById(Long sessionId);
    boolean isExpired(LocalDateTime startTime);
}
