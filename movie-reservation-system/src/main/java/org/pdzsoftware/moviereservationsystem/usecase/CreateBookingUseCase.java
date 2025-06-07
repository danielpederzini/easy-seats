package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.request.BookingRequest;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.springframework.stereotype.Service;

@Service
public interface CreateBookingUseCase {
    BookingResponse execute(Long userId, BookingRequest bookingRequest);
}
