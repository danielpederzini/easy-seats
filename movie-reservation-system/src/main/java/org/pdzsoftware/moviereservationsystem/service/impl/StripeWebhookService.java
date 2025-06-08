package org.pdzsoftware.moviereservationsystem.service.impl;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.pdzsoftware.moviereservationsystem.service.BookingStatusService;
import org.springframework.stereotype.Service;

import static org.pdzsoftware.moviereservationsystem.service.PaymentService.BOOKING_ID_KEY;
import static org.pdzsoftware.moviereservationsystem.service.PaymentService.USER_ID_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {
    public static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    public static final String CHECKOUT_SESSION_EXPIRED = "checkout.session.expired";
    public static final String PAYMENT_INTENT_PAYMENT_FAILED = "payment_intent.payment_failed";
    public static final String REFUND_UPDATED = "refund.updated";

    private final BookingService bookingService;
    private final BookingStatusService bookingStatusService;

    public void handleStripeEvent(Event event) {
        String type = event.getType();

        log.info("[StripeWebhookService] Received stripe event of type: {}", type);

        switch (type) {
            case CHECKOUT_SESSION_COMPLETED:
                handleCheckoutCompleted(event);
                break;
            case PAYMENT_INTENT_SUCCEEDED:
                handlePaymentSuccess(event);
                break;
            case CHECKOUT_SESSION_EXPIRED:
                handleCheckoutExpired(event);
                break;
            case PAYMENT_INTENT_PAYMENT_FAILED:
                handlePaymentFailed(event);
                break;
            case REFUND_UPDATED:
                handleRefundUpdate(event);
                break;
            default:
                log.warn("[StripeWebhookService] Stripe event ignored, type not recognized: {}", type);
                return;
        }

        log.info("[StripeWebhookService] Successfully handled stripe event of type: {}", type);
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getData().getObject();

        Long bookingId = Long.valueOf(session.getMetadata().get(BOOKING_ID_KEY));
        Long userId = Long.valueOf(session.getMetadata().get(USER_ID_KEY));
        Booking booking = getBooking(bookingId, userId);

        bookingStatusService.handleCheckoutCompleted(booking, session.getId(), session.getPaymentIntent());
    }

    private void handlePaymentSuccess(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();

        Long bookingId = Long.valueOf(paymentIntent.getMetadata().get(BOOKING_ID_KEY));
        Long userId = Long.valueOf(paymentIntent.getMetadata().get(USER_ID_KEY));
        Booking booking = getBooking(bookingId, userId);

        bookingStatusService.handlePaymentSuccess(booking, paymentIntent.getId());
    }

    private void handleCheckoutExpired(Event event) {
        Session checkoutSession = (Session) event.getData().getObject();

        Long bookingId = Long.valueOf(checkoutSession.getMetadata().get(BOOKING_ID_KEY));
        Long userId = Long.valueOf(checkoutSession.getMetadata().get(USER_ID_KEY));
        Booking booking = getBooking(bookingId, userId);

        bookingStatusService.handleCheckoutExpired(booking, checkoutSession.getId());
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();

        Long bookingId = Long.valueOf(paymentIntent.getMetadata().get(BOOKING_ID_KEY));
        Long userId = Long.valueOf(paymentIntent.getMetadata().get(USER_ID_KEY));
        Booking booking = getBooking(bookingId, userId);

        bookingStatusService.handlePaymentFailed(booking, paymentIntent.getId());
    }

    private void handleRefundUpdate(Event event) {
        Refund refund = (Refund) event.getData().getObject();

        // Only process successful refund updates
        if ("succeeded".equals(refund.getStatus())) {
            Long bookingId = Long.valueOf(refund.getMetadata().get(BOOKING_ID_KEY));
            Long userId = Long.valueOf(refund.getMetadata().get(USER_ID_KEY));
            Booking booking = getBooking(bookingId, userId);

            bookingStatusService.handlePaymentRefunded(booking, refund.getId());
        }
    }

    private Booking getBooking(Long bookingId, Long userId) {
        return bookingService.findByIdAndUserId(bookingId, userId).orElseThrow(() -> {
            log.error("[StripeWebhookService] Booking not found for ID: {} and user ID: {}", bookingId, userId);
            return new NotFoundException("Booking not found for given ID and user ID");
        });
    }
}
