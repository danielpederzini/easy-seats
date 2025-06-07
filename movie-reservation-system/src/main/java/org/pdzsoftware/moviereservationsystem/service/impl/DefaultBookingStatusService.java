package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingStatusUpdatedEvent;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.repository.BookingRepository;
import org.pdzsoftware.moviereservationsystem.service.BookingStatusService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultBookingStatusService implements BookingStatusService {
    private static final String ORIGIN_ID = "default-booking-status-service";

    private final BookingRepository bookingRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void validateAndUpdateStatus(Booking booking, BookingStatus newStatus) {
        // TODO: maybe adding a fetch for the booking status from the database, in case other thread altered it
        BookingStatus currentStatus = booking.getBookingStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            log.error("[DefaultBookingStatusService] Booking with ID: {} and status: {} cannot transition to status: {}",
                    booking.getId(), currentStatus, newStatus);
            throw new ConflictException("Booking cannot transition from " + currentStatus + " to " + newStatus);
        }

        booking.setBookingStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        log.info("[DefaultBookingStatusService] Booking with ID: {} updated from status: {} to status: {}",
                booking.getId(), currentStatus, newStatus);

        eventPublisher.publishEvent(new BookingStatusUpdatedEvent(booking, ORIGIN_ID));
    }

    @Override
    public void handleCheckoutCompleted(Booking booking, String checkoutId, String paymentIntentId) {
        String bookingCheckoutId = booking.getCheckoutId();

        // Assure idempotence and prevent unnecessary retries
        if (booking.getPaymentIntentId() != null) return;

        checkCheckoutIdsMatch(booking.getId(), checkoutId, bookingCheckoutId);

        booking.setPaymentIntentId(paymentIntentId);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        log.info("[DefaultBookingStatusService] Booking with ID: {} updated with paymentIntentId: {} from checkout completion event",
                booking.getId(), paymentIntentId);
    }

    @Override
    public void handleCheckoutExpired(Booking booking, String checkoutId) {
        String bookingCheckoutId = booking.getCheckoutId();

        // Assure idempotence and prevent unnecessary retries
        if (booking.getBookingStatus().equals(EXPIRED)) return;

        checkCheckoutIdsMatch(booking.getId(), checkoutId, bookingCheckoutId);

        validateAndUpdateStatus(booking, EXPIRED);
    }

    @Override
    public void handlePaymentSuccess(Booking booking, String paymentIntentId) {
        String bookingPaymentIntentId = booking.getPaymentIntentId();

        // Assure idempotence and prevent unnecessary retries
        // The job runner will handle the refund logic for EXPIRED entries with confirmed payments
        if (List.of(PAYMENT_CONFIRMED, EXPIRED).contains(booking.getBookingStatus())) return;

        // Payment confirmation can arrive before checkoutCompleted event, thus bookingPaymentIntentId can be null
        if (bookingPaymentIntentId != null) {
            checkPaymentIntentIdsMatch(booking.getId(), paymentIntentId, bookingPaymentIntentId);
        } else {
            booking.setPaymentIntentId(paymentIntentId);
            log.info("[DefaultBookingStatusService] Booking with ID: {} updated with paymentIntentId: {} from payment succeeded event",
                    booking.getId(), paymentIntentId);
        }

        validateAndUpdateStatus(booking, PAYMENT_CONFIRMED);
    }

    @Override
    public void handlePaymentFailed(Booking booking, String paymentIntentId) {
        String bookingPaymentIntentId = booking.getPaymentIntentId();

        // If payment fails more than once, just change updatedAt
        if (booking.getBookingStatus().equals(PAYMENT_RETRY)) {
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            return;
        }

        // Payment failure can arrive before checkoutCompleted event, thus paymentIntentId can be null
        if (bookingPaymentIntentId != null) {
            checkPaymentIntentIdsMatch(booking.getId(), paymentIntentId, bookingPaymentIntentId);
        } else {
            log.info("[DefaultBookingStatusService] Booking with ID: {} updated with paymentIntentId: {} from payment failed event",
                    booking.getId(), paymentIntentId);
            booking.setPaymentIntentId(paymentIntentId);
        }

        validateAndUpdateStatus(booking, PAYMENT_RETRY);
    }

    @Override
    public void handlePaymentRefunded(Booking booking, String refundId) {
        String bookingRefundId = booking.getRefundId();

        // Assure idempotence and prevent unnecessary retries
        if (booking.getBookingStatus().equals(CANCELLED)) return;

        checkRefundIdsMatch(booking, refundId, bookingRefundId);

        validateAndUpdateStatus(booking, CANCELLED);
    }

    private static void checkCheckoutIdsMatch(Long bookingId, String checkoutId, String bookingCheckoutId) {
        if (!bookingCheckoutId.equals(checkoutId)) {
            log.error("[DefaultBookingStatusService] Checkout session ID: {} does not match: {} from booking with ID: {}",
                    checkoutId, bookingCheckoutId, bookingId);
            throw new NotFoundException("Booking checkout ID does not match");
        }
    }

    private static void checkPaymentIntentIdsMatch(Long bookingId, String paymentIntentId, String bookingPaymentIntentId) {
        if (!bookingPaymentIntentId.equals(paymentIntentId)) {
            log.error("[DefaultBookingStatusService] Payment intent ID: {} does not match: {} from booking with ID: {}",
                    paymentIntentId, bookingPaymentIntentId, bookingId);
            throw new NotFoundException("Booking payment intent ID does not match");
        }
    }

    private static void checkRefundIdsMatch(Booking booking, String refundId, String bookingRefundId) {
        if (!bookingRefundId.equals(refundId)) {
            log.error("[DefaultBookingStatusService] Refund ID: {} does not match: {} from booking with ID: {}",
                    refundId, bookingRefundId, booking.getId());
            throw new NotFoundException("Booking refund ID does not match");
        }
    }
}
