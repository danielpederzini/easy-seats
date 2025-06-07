package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.pdzsoftware.moviereservationsystem.dto.response.BookedSeatResponse;
import org.pdzsoftware.moviereservationsystem.repository.BookedSeatRepository;
import org.pdzsoftware.moviereservationsystem.service.BookedSeatService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultBookedSeatService implements BookedSeatService {
    private final BookedSeatRepository bookedSeatRepository;

    @Override
    public List<BookedSeatResponse> findResponsesByBookingId(Long bookingId) {
        return bookedSeatRepository.findResponseByBookingId(bookingId);
    }
}

