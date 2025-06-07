package org.pdzsoftware.moviereservationsystem.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.PaymentInfoDto;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.repository.BookedSeatRepository;
import org.pdzsoftware.moviereservationsystem.repository.BookingRepository;
import org.pdzsoftware.moviereservationsystem.service.BookingStatusService;
import org.pdzsoftware.moviereservationsystem.service.PaymentService;
import org.pdzsoftware.moviereservationsystem.usecase.CancelBookingUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.*;
import static org.pdzsoftware.moviereservationsystem.enums.CheckoutStatus.COMPLETED;
import static org.pdzsoftware.moviereservationsystem.enums.CheckoutStatus.PENDING;
import static org.pdzsoftware.moviereservationsystem.enums.PaymentStatus.SUCCEEDED;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingJobRunner {
    private static final long ALIGN_EXPIRED_STATUS_RATE_MS = 10 * 60 * 1000L;
    private static final long MARK_AS_PAST_RATE_MS = 24 * 60 * 60 * 1000L;
    private static final long DELETE_BOOKINGS_RATE_MS = 7 * 24 * 60 * 60 * 1000L;
    private static final Duration DELETION_THRESHOLD = Duration.ofDays(7);

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final BookingStatusService bookingStatusService;
    private final PaymentService paymentService;

    private final CancelBookingUseCase cancelBooking;

    public void tryExpiringBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.error("[BookingJobRunner] Cannot find booking with ID: {}", bookingId);
            return new NotFoundException("Booking not found for given ID");
        });

        if (!Set.of(AWAITING_PAYMENT, PAYMENT_RETRY).contains(booking.getBookingStatus())) {
            return; // Nothing to do here
        }

        PaymentInfoDto paymentInfo = getPaymentInfoOrElseExpire(booking);
        if (paymentInfo == null) return;

        // If there's a successful payment for the booking, confirm it instead of expiring
        if (paymentInfo.getPaymentStatus().equals(SUCCEEDED)) {
            log.info("[BookingJobRunner] Found successful payment for booking with ID: {}", booking.getId());
            if (booking.getPaymentIntentId() == null) {
                booking.setPaymentIntentId(paymentInfo.getPaymentIntentId());
            }
            bookingStatusService.validateAndUpdateStatus(booking, PAYMENT_CONFIRMED);
            return;
        }

        if (paymentInfo.getCheckoutStatus().equals(COMPLETED)) {
            log.info("[BookingJobRunner] Found completed checkout session with failed payment for booking with ID: {}", booking.getId());
            bookingStatusService.validateAndUpdateStatus(booking, EXPIRED);
        } else {
            expireThroughWebhookOrElseDirectly(booking);
        }
    }

    private PaymentInfoDto getPaymentInfoOrElseExpire(Booking booking) {
        try {
            return paymentService.getPaymentInfoBySessionId(booking.getCheckoutId());
        } catch (Exception ex) {
            log.error("[BookingJobRunner] Error getting payment info for booking with ID: {} through payment provider",
                    booking.getId(), ex);
            log.info("[BookingJobRunner] Expiring booking with ID: {} directly in database due to lack of payment info",
                    booking.getId());

            // This can cause mistakenly expired bookings, which will be refunded by the other job
            bookingStatusService.validateAndUpdateStatus(booking, EXPIRED);
            return null;
        }
    }

    private void expireThroughWebhookOrElseDirectly(Booking booking) {
        try {
            // This will send a webhook expiration event if successful
            paymentService.expireCheckoutSession(booking.getCheckoutId());
            log.info("[BookingJobRunner] Successfully expired checkout session for booking with ID: {}", booking.getId());
        } catch (Exception ex) {
            log.error("[BookingJobRunner] Error expiring checkout session for booking with ID: {}", booking.getId(), ex);
            log.info("[BookingJobRunner] Expiring booking with ID: {} directly in database",
                    booking.getId());
            bookingStatusService.validateAndUpdateStatus(booking, EXPIRED);
        }
    }

    // Refund bookings that were expired but have an existing payment, and mark the rest for deletion
    @Scheduled(fixedRate = ALIGN_EXPIRED_STATUS_RATE_MS)
    public void alignExpiredStatus() {
        List<Booking> bookings = bookingRepository.findExpiredBookings();

        bookings.forEach(booking -> {
            PaymentInfoDto paymentInfo = tryGettingPaymentInfo(booking.getCheckoutId());

            if (paymentInfo.getPaymentStatus().equals(SUCCEEDED)) {
                log.info("[BookingJobRunner] Found successful payment for expired booking with ID: {}",
                        booking.getId());
                // Cancelling so user gets refunded. The seats got freed on expiration, so can't confirm the booking
                cancelBooking.execute(booking);
            } else if (!paymentInfo.getCheckoutStatus().equals(PENDING)) {
                log.info("[BookingJobRunner] Found closed checkout with no successful payment for expired booking with ID: {}",
                        booking.getId());
                bookingStatusService.validateAndUpdateStatus(booking, AWAITING_DELETION);
            }
        });
    }

    private PaymentInfoDto tryGettingPaymentInfo(String checkoutId) {
        try {
            return paymentService.getPaymentInfoBySessionId(checkoutId);
        } catch (Exception ex) {
            log.error("[BookingJobRunner] Error getting payment info for checkout session with ID: {}",
                    checkoutId, ex);
            throw new InternalErrorException("Internal error getting payment info for checkout session");
        }
    }

    // Mark any bookings whose sessions already ended as past
    @Scheduled(fixedRate = MARK_AS_PAST_RATE_MS)
    @Transactional(rollbackFor = Exception.class)
    public void markBookingsAsPastIfSessionEnded() {
        int bookingsMarkedAsPast = bookingRepository.markBookingsAsPastIfSessionEnded(LocalDateTime.now());

        if (bookingsMarkedAsPast > 0) {
            log.info("[BookingJobRunner] Marked {} bookings as past", bookingsMarkedAsPast);
        }
    }

    // Delete bookings marked for deletion, with a configurable date threshold
    @Scheduled(fixedRate = DELETE_BOOKINGS_RATE_MS)
    @Transactional(rollbackFor = Exception.class)
    public void deleteMarkedBookings() {
        LocalDateTime threshold = LocalDateTime.now().minus(DELETION_THRESHOLD);

        Set<Long> bookingIdsToDelete = bookingRepository.findIdsToDelete(threshold);

        if (!bookingIdsToDelete.isEmpty()) {
            Set<Long> seatIdsToDelete = bookedSeatRepository.findIdsToDelete(bookingIdsToDelete);

            bookedSeatRepository.deleteAllByIdInBatch(seatIdsToDelete);
            bookingRepository.deleteAllByIdInBatch(bookingIdsToDelete);

            log.info("[BookingJobRunner] Deleted {} booking(s) marked for deletion from {} and before. Deleted IDs: {}",
                    bookingIdsToDelete.size(), threshold, bookingIdsToDelete);
        }
    }
}
