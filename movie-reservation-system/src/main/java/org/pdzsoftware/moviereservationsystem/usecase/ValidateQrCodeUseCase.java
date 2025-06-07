package org.pdzsoftware.moviereservationsystem.usecase;

import org.springframework.stereotype.Service;

@Service
public interface ValidateQrCodeUseCase {
    void execute(String qrCode);
}
