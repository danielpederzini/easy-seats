package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.pdzsoftware.moviereservationsystem.service.BookingStatusService;
import org.pdzsoftware.moviereservationsystem.service.PaymentService;
import org.pdzsoftware.moviereservationsystem.service.UserService;
import org.pdzsoftware.moviereservationsystem.usecase.CancelBookingUseCase;
import org.springframework.stereotype.Service;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.AWAITING_CANCELLATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCancelBookingUseCase implements CancelBookingUseCase {
    private final UserService userService;
    private final BookingService bookingService;
    private final BookingStatusService bookingStatusService;
    private final PaymentService paymentService;

    @Override
    public void execute(Long userId, Long bookingId) {
        Booking booking = bookingService.findByIdAndUserId(bookingId, userId).orElseThrow(() -> {
            log.error("[DefaultCancelBookingUseCase] Booking not found for ID: {} and user ID: {}", bookingId, userId);
            return new NotFoundException("Booking not found for given ID and user ID");
        });

        cancelAndRefund(userId, booking);
    }

    @Override
    public void execute(Booking booking) {
        Long userId = userService.findIdByBookingId(booking.getId()).orElseThrow(() -> {
            log.error("[DefaultCancelBookingUseCase] User not found for booking ID: {}", booking.getId());
            return new NotFoundException("User not found for given booking ID");
        });

        cancelAndRefund(userId, booking);
    }

    private void cancelAndRefund(Long userId, Booking booking) {
        String refundId = tryCreatingRefund(userId, booking);

        booking.setRefundId(refundId);
        bookingStatusService.validateAndUpdateStatus(booking, AWAITING_CANCELLATION);
    }

    private String tryCreatingRefund(Long userId, Booking booking) {
        try {
            log.info("[DefaultCancelBookingUseCase] Creating refund for booking with ID: {} and paymentIntentId: {}",
                    booking.getId(), booking.getPaymentIntentId());
            return paymentService.createRefund(booking, userId);
        } catch (Exception e) {
            log.error("[DefaultCancelBookingUseCase] Error creating refund for booking with ID: {} and paymentIntentId: {}",
                    booking.getId(), booking.getPaymentIntentId(), e);
            throw new InternalErrorException("Internal error creating refund");
        }
    }
}
