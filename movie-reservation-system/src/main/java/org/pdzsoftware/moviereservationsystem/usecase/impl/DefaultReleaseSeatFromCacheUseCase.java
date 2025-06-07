package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.CacheSeatStatusUpdateEvent;
import org.pdzsoftware.moviereservationsystem.service.SeatService;
import org.pdzsoftware.moviereservationsystem.usecase.ReleaseSeatFromCacheUseCase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultReleaseSeatFromCacheUseCase implements ReleaseSeatFromCacheUseCase {
    private final SeatService seatService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Long userId, Long sessionId, Long seatId, String clientId) {
        seatService.releaseFromCache(userId, sessionId, seatId);

        eventPublisher.publishEvent(new CacheSeatStatusUpdateEvent(
                seatId, sessionId, clientId, false
        ));
    }
}
