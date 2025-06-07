package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.model.*;
import org.pdzsoftware.moviereservationsystem.repository.BookingRepository;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultBookingService implements BookingService {
    private final BookingRepository bookingRepository;

    @Override
    public Booking createAndSaveBooking(User user,
                                        Session session,
                                        List<Seat> seats) {

        LocalDateTime now = LocalDateTime.now();

        Booking booking = Booking.builder()
                .bookingStatus(AWAITING_PAYMENT)
                .createdAt(now)
                .updatedAt(now)
                .user(user)
                .session(session)
                .build();

        List<BookedSeat> bookedSeats = generateBookedSeatEntities(booking, seats);
        booking.setBookedSeats(bookedSeats);
        booking.setTotalPrice(calculateTotalPrice(bookedSeats));

        return bookingRepository.save(booking);
    }

    @Override
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> findByIdAndUserId(Long bookingId, Long userId) {
        return bookingRepository.findByIdAndUserId(bookingId, userId);
    }

    @Override
    public Page<BookingDetailedResponse> findDetailedByFilters(Long userId,
                                                               List<BookingStatus> statuses,
                                                               int page) {
        Pageable pageable = PageRequest.of(page, 15);

        // Default: all but expired
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(
                    AWAITING_PAYMENT,
                    PAYMENT_RETRY,
                    PAYMENT_CONFIRMED,
                    AWAITING_CANCELLATION,
                    CANCELLED,
                    PAST
            );
        }

        return bookingRepository.findDetailedByFilters(userId, statuses, pageable);
    }

    private List<BookedSeat> generateBookedSeatEntities(Booking booking, List<Seat> seats) {
        List<BookedSeat> bookedSeats = new ArrayList<>();
        Session session = booking.getSession();

        LocalDateTime now = LocalDateTime.now();

        for (Seat s : seats) {
            BigDecimal seatPrice = switch (s.getSeatType()) {
                case STANDARD -> session.getStandardSeatPrice();
                case VIP -> session.getVipSeatPrice();
                case PWD -> session.getPwdSeatPrice();
            };

            BookedSeat bookedSeat = BookedSeat.builder()
                    .seatPrice(seatPrice)
                    .createdAt(now)
                    .updatedAt(now)
                    .booking(booking)
                    .seat(s)
                    .build();

            bookedSeats.add(bookedSeat);
        }

        return bookedSeats;
    }

    private BigDecimal calculateTotalPrice(List<BookedSeat> bookedSeats) {
        return bookedSeats.stream()
                .map(BookedSeat::getSeatPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
