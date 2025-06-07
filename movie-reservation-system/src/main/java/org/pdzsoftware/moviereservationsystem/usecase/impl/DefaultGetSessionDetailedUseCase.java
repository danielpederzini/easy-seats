package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SeatResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.GoneException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.service.MovieService;
import org.pdzsoftware.moviereservationsystem.service.SeatService;
import org.pdzsoftware.moviereservationsystem.service.SessionService;
import org.pdzsoftware.moviereservationsystem.usecase.GetSessionDetailedUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGetSessionDetailedUseCase implements GetSessionDetailedUseCase {
    private final SessionService sessionService;
    private final MovieService movieService;
    private final SeatService seatService;

    @Override
    public SessionDetailedResponse execute(Long sessionId) {
        SessionDetailedResponse detailedResponse = sessionService.findDetailedResponseById(sessionId).orElseThrow(() -> {
            log.error("[DefaultGetSessionDetailedUseCase] Session not found for ID: {}", sessionId);
            return new NotFoundException("Session not found for given ID");
        });

        if (sessionService.isExpired(detailedResponse.getStartTime())) {
            log.error("[DefaultGetSessionDetailedUseCase] Session with ID: {} is expired", sessionId);
            throw new GoneException("Session is expired");
        }

        MovieResponse movieResponse = movieService.findResponseBySessionId(sessionId).orElseThrow(() -> {
            log.error("[DefaultGetSessionDetailedUseCase] Movie not found for session ID: {}", sessionId);
            return new NotFoundException("Movie not found for given session ID");
        });

        List<SeatResponse> seatResponses = seatService.findResponsesBySessionId(sessionId);

        if (seatResponses.isEmpty()) {
            log.error("[DefaultGetSessionDetailedUseCase] Couldn't find any seats for session ID: {}", sessionId);
            throw new NotFoundException("No seats found for given session ID");
        }

        Set<Long> seatIds = seatResponses.stream().map(SeatResponse::getId).collect(Collectors.toSet());
        Set<Long> takenSeatIds = seatService.getTakenSeatIdsForSession(seatIds, sessionId);

        seatResponses.forEach(seatResponse ->
            seatResponse.setTaken(takenSeatIds.contains(seatResponse.getId()))
        );

        detailedResponse.setMovie(movieResponse);
        detailedResponse.setSeats(seatResponses);

        return detailedResponse;
    }
}
