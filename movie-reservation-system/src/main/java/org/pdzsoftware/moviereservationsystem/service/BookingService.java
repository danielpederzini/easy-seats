package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.model.Seat;
import org.pdzsoftware.moviereservationsystem.model.Session;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface BookingService {
    Booking createAndSaveBooking(User user, Session session, List<Seat> seats);
    Booking saveBooking(Booking booking);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Page<BookingDetailedResponse> findDetailedByFilters(Long userId, List<BookingStatus> statuses, int page);
}
