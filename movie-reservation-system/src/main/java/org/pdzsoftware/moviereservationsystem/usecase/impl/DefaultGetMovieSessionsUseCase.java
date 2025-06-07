package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.pdzsoftware.moviereservationsystem.service.SeatService;
import org.pdzsoftware.moviereservationsystem.service.SessionService;
import org.pdzsoftware.moviereservationsystem.usecase.GetMovieSessionsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGetMovieSessionsUseCase implements GetMovieSessionsUseCase {
    private final SessionService sessionService;
    private final SeatService seatService;

    @Override
    public Page<SessionResponse> execute(Long movieId, LocalDate sessionDate, Long theaterId, int page) {
        Page<SessionResponse> sessions = sessionService.findResponsesByFilters(movieId, sessionDate, theaterId, page);

        Set<Long> sessionIds = sessions.getContent().stream()
                .map(SessionResponse::getId).collect(Collectors.toSet());

        Map<Long, Boolean> sessionHasFreeSeats = seatService.getSeatAvailabilityForSessions(sessionIds);

        sessions.getContent().forEach(session ->
            session.setHasFreeSeats(sessionHasFreeSeats.getOrDefault(session.getId(), true))
        );

        return sessions;
    }
}
