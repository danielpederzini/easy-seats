package org.pdzsoftware.moviereservationsystem.usecase;

import org.springframework.stereotype.Service;

@Service
public interface CreateQrCodeUseCase {
    byte[] execute(Long bookingId, Long userId);
}
