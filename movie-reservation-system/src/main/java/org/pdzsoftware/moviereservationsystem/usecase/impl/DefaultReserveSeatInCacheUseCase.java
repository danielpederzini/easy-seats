package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.CacheSeatStatusUpdateEvent;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.service.SeatService;
import org.pdzsoftware.moviereservationsystem.service.SessionService;
import org.pdzsoftware.moviereservationsystem.service.UserService;
import org.pdzsoftware.moviereservationsystem.usecase.ReserveSeatInCacheUseCase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultReserveSeatInCacheUseCase implements ReserveSeatInCacheUseCase {
    private final UserService userService;
    private final SessionService sessionService;
    private final SeatService seatService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Long userId, Long sessionId, Long seatId, String clientId) {
        if (!userService.existsById(userId)) {
            log.error("[DefaultReserveSeatInCacheUseCase] User not found for ID: {}", userId);
            throw new NotFoundException("User not found for given ID");
        }

        if (!sessionService.existsById(sessionId)) {
            log.error("[DefaultReserveSeatInCacheUseCase] Session not found for ID: {}", sessionId);
            throw new NotFoundException("Session not found for given ID");
        }

        if (!seatService.existsByIdAndSessionId(seatId, sessionId)) {
            log.error("[DefaultReserveSeatInCacheUseCase] Seat not found for ID: {} and session ID: {}", seatId, sessionId);
            throw new NotFoundException("Seat not found for given ID and session ID");
        }

        seatService.reserveInCache(userId, sessionId, seatId);

        eventPublisher.publishEvent(new CacheSeatStatusUpdateEvent(
                seatId, sessionId, clientId, true
        ));
    }
}
