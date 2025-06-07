package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingCreatedEvent;
import org.pdzsoftware.moviereservationsystem.dto.request.BookingRequest;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.GoneException;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.model.Seat;
import org.pdzsoftware.moviereservationsystem.model.Session;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.service.*;
import org.pdzsoftware.moviereservationsystem.usecase.CreateBookingUseCase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCreateBookingUseCase implements CreateBookingUseCase {
    private static final String ORIGIN_ID = "default-create-booking-use-case";
    private static final long EXPIRES_IN_MS = 10 * 60 * 1000L;

    private final BookingService bookingService;
    private final UserService userService;
    private final SessionService sessionService;
    private final SeatService seatService;
    private final PaymentService paymentService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingResponse execute(Long userId, BookingRequest bookingRequest) {
        User user = userService.findById(userId).orElseThrow(() -> {
            log.error("[DefaultCreateBookingUseCase] User not found for ID: {}", userId);
            return new NotFoundException("User not found for given ID");
        });

        Session session = sessionService.findById(bookingRequest.getSessionId()).orElseThrow(() -> {
            log.error("[DefaultCreateBookingUseCase] Session not found for ID: {}", bookingRequest.getSessionId());
            return new NotFoundException("Session not found for given ID");
        });

        if (sessionService.isExpired(session.getStartTime())) {
            log.warn("[DefaultCreateBookingUseCase] Session with ID: {} is expired, booking not created", session.getId());
            throw new GoneException("Session is expired");
        }

        List<Seat> seats = seatService.findByIds(bookingRequest.getSeatIds(), session.getId());
        if (seats.size() != bookingRequest.getSeatIds().size()) {
            log.error("[DefaultCreateBookingUseCase] Couldn't find all seats for IDs: {} and session ID: {}. Only found: {}",
                    bookingRequest.getSeatIds(), session.getId(), seats);
            throw new NotFoundException("Seats not found for given IDs and session ID");
        }

        boolean allAvailable = seatService.areAllAvailableToBook(session.getId(), user.getId(), bookingRequest.getSeatIds());

        if (!allAvailable) {
            log.error("[DefaultCreateBookingUseCase] User tried to book unavailable seats, this should not happen");
            throw new ConflictException("User tried to book unavailable seats, this should not happen");
        }

        Booking booking = bookingService.createAndSaveBooking(user, session, seats);

        BookingResponse response = tryCreatingCheckout(bookingRequest, user, booking, session);

        // Update entity with checkout session info
        booking.setCheckoutId(response.getCheckoutId());
        booking.setCheckoutUrl(response.getCheckoutURL());
        booking.setExpiresAt(LocalDateTime.now().plus(EXPIRES_IN_MS, ChronoUnit.MILLIS));
        bookingService.saveBooking(booking);

        log.info("[DefaultCreateBookingUseCase] Booking with ID: {} created for user with ID: {} and session ID: {}",
                booking.getId(), user.getId(), session.getId());

        eventPublisher.publishEvent(new BookingCreatedEvent(
                userId, session.getId(), booking, ORIGIN_ID, bookingRequest.getSeatIds()
        ));

        return response;
    }

    private BookingResponse tryCreatingCheckout(BookingRequest bookingRequest, User user, Booking booking, Session session) {
        try {
            return paymentService.createCheckout(
                    user, booking,
                    bookingRequest.getSuccessUrl(),
                    bookingRequest.getCancelUrl(),
                    session.getMovie().getTitle()
            );
        } catch (Exception e) {
            log.error("[DefaultCreateBookingUseCase] Error creating checkout session for booking with ID: {}", booking.getId(), e);
            throw new InternalErrorException("Internal error creating checkout session");
        }
    }
}
