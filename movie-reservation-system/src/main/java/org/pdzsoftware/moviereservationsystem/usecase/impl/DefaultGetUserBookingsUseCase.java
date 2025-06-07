package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.BookedSeatResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.service.BookedSeatService;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.pdzsoftware.moviereservationsystem.service.MovieService;
import org.pdzsoftware.moviereservationsystem.service.SessionService;
import org.pdzsoftware.moviereservationsystem.usecase.GetUserBookingsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGetUserBookingsUseCase implements GetUserBookingsUseCase {
    private final BookingService bookingService;
    private final SessionService sessionService;
    private final MovieService movieService;
    private final BookedSeatService bookedSeatService;

    @Override
    public Page<BookingDetailedResponse> execute(Long userId, List<BookingStatus> statuses, int page) {
        Page<BookingDetailedResponse> detailedResponses = bookingService.findDetailedByFilters(userId, statuses, page);

        // TODO: batch these calls to improve response time
        detailedResponses.getContent().forEach(booking -> {
            SessionResponse session = sessionService.findResponseByBookingId(booking.getId()).orElseThrow(() -> {
                log.error("[DefaultGetUserBookingsUseCase] Session not found for booking ID: {}", booking.getId());
                return new NotFoundException("Session not found for given booking ID");
            });

            MovieResponse movie = movieService.findResponseBySessionId(session.getId()).orElseThrow(() -> {
                log.error("[DefaultGetUserBookingsUseCase] Movie not found for session ID: {}", session.getId());
                return new NotFoundException("Movie not found for given session ID");
            });

            List<BookedSeatResponse> bookedSeats = bookedSeatService.findResponsesByBookingId(booking.getId());

            booking.setSession(session);
            booking.setMovie(movie);
            booking.setBookedSeats(bookedSeats);
        });

        return detailedResponses;
    }
}
