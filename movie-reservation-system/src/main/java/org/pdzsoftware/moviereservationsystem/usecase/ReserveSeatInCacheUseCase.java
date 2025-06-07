package org.pdzsoftware.moviereservationsystem.usecase;

import org.springframework.stereotype.Service;

@Service
public interface ReserveSeatInCacheUseCase {
    void execute(Long userId, Long sessionId, Long seatId, String clientId);
}
