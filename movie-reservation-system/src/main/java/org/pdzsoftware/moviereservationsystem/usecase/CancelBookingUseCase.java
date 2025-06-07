package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.springframework.stereotype.Service;

@Service
public interface CancelBookingUseCase {
    void execute(Long userId, Long bookingId);
    void execute(Booking booking);
}
