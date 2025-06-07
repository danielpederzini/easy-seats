package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.response.BookedSeatResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookedSeatService {
    List<BookedSeatResponse> findResponsesByBookingId(Long bookingId);
}
