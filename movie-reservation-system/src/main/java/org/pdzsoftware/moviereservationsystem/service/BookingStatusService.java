package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.springframework.stereotype.Service;

@Service
public interface BookingStatusService {
    void validateAndUpdateStatus(Booking booking, BookingStatus newStatus);

    void handleCheckoutCompleted(Booking booking, String checkoutId, String paymentIntentId);
    void handleCheckoutExpired(Booking booking, String checkoutId);
    void handlePaymentSuccess(Booking booking, String paymentIntentId);
    void handlePaymentFailed(Booking booking, String paymentIntentId);
    void handlePaymentRefunded(Booking booking, String refundId);
}
