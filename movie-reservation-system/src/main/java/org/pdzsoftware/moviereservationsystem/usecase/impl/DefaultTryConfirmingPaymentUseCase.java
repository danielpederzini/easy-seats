package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.PaymentInfoDto;
import org.pdzsoftware.moviereservationsystem.dto.request.CheckoutInfoRequest;
import org.pdzsoftware.moviereservationsystem.enums.PaymentStatus;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.pdzsoftware.moviereservationsystem.service.BookingStatusService;
import org.pdzsoftware.moviereservationsystem.service.PaymentService;
import org.pdzsoftware.moviereservationsystem.usecase.TryConfirmingPaymentUseCase;
import org.springframework.stereotype.Service;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.PAYMENT_CONFIRMED;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultTryConfirmingPaymentUseCase implements TryConfirmingPaymentUseCase {
    private final BookingService bookingService;
    private final BookingStatusService bookingStatusService;
    private final PaymentService paymentService;

    @Override
    public boolean execute(Long userId, Long bookingId, CheckoutInfoRequest checkoutInfoRequest) {
        Booking booking = bookingService.findByIdAndUserId(bookingId, userId).orElseThrow(() -> {
            log.error("[DefaultTryConfirmingPaymentUseCase] Booking not found for ID: {} and user ID: {}",
                    bookingId, userId);
            return new NotFoundException("Booking not found for given ID and user ID");
        });

        assertCheckoutIdsMatch(bookingId, checkoutInfoRequest.getCheckoutId(), booking.getCheckoutId());

        if (booking.getBookingStatus().equals(PAYMENT_CONFIRMED)) {
            return true;
        }

        // Reaching payment provider for active confirmation
        PaymentInfoDto paymentInfo = tryGettingPaymentInfo(checkoutInfoRequest.getCheckoutId());

        if (!paymentInfo.getPaymentStatus().equals(PaymentStatus.SUCCEEDED)) {
            return false;
        }

        log.info("[DefaultTryConfirmingPaymentUseCase] Found payment confirmation for pending booking with ID: {}",
                booking.getId());

        if (booking.getPaymentIntentId() != null) {
            assertPaymentIntentIdsMatch(booking.getId(), paymentInfo.getPaymentIntentId(), booking.getPaymentIntentId());
        } else {
            log.info("[DefaultTryConfirmingPaymentUseCase] Booking with ID: {} updated with paymentIntentId: {} from active confirmation",
                    booking.getId(), paymentInfo.getPaymentIntentId());
            booking.setPaymentIntentId(paymentInfo.getPaymentIntentId());
        }

        bookingStatusService.validateAndUpdateStatus(booking, PAYMENT_CONFIRMED);
        return true;
    }

    private PaymentInfoDto tryGettingPaymentInfo(String checkoutId) {
        try {
            return paymentService.getPaymentInfoBySessionId(checkoutId);
        } catch (Exception ex) {
            log.error("[DefaultTryConfirmingPaymentUseCase] Error getting payment info for checkout ID: {}",
                    checkoutId, ex);
            throw new InternalErrorException("Internal error getting payment info for booking");
        }
    }

    private static void assertCheckoutIdsMatch(Long bookingId, String checkoutId, String bookingCheckoutId) {
        if (!bookingCheckoutId.equals(checkoutId)) {
            log.error("[DefaultTryConfirmingPaymentUseCase] Checkout ID: {} does not match: {} from booking with ID: {}",
                    checkoutId, bookingCheckoutId, bookingId);
            throw new NotFoundException("Booking checkout ID does not match");
        }
    }

    private static void assertPaymentIntentIdsMatch(Long bookingId, String paymentIntentId, String bookingPaymentIntentId) {
        if (!bookingPaymentIntentId.equals(paymentIntentId)) {
            log.error("[DefaultTryConfirmingPaymentUseCase] Payment intent ID: {} does not match: {} from booking with ID: {}",
                    paymentIntentId, bookingPaymentIntentId, bookingId);
            throw new NotFoundException("Booking payment intent ID does not match");
        }
    }
}
