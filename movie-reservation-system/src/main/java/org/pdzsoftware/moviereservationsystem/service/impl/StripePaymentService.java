package org.pdzsoftware.moviereservationsystem.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.PaymentIntentData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.PaymentInfoDto;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.pdzsoftware.moviereservationsystem.enums.CheckoutStatus;
import org.pdzsoftware.moviereservationsystem.enums.PaymentStatus;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripePaymentService implements PaymentService {
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public BookingResponse createCheckout(User user,
                                          Booking booking,
                                          String successUrl,
                                          String cancelUrl,
                                          String movieTitle) throws StripeException {
        String productName = String.format("%s movie ticket(s) for %s", booking.getBookedSeats().size(), movieTitle);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?bookingId=" + booking.getId() + "&checkoutId={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setExpiresAt(Instant.now().getEpochSecond() + 1800) // Min is 30 minutes, so we expire earlier
                .setCustomerEmail(user.getEmail())
                .addLineItem(createLineItem(booking, productName))
                .setPaymentIntentData(createPaymentIntentData(user.getId(), booking.getId()))
                .putMetadata(BOOKING_ID_KEY, String.valueOf(booking.getId()))
                .putMetadata(USER_ID_KEY, String.valueOf(user.getId()))
                .build();

        Session session = Session.create(params);

        log.info("[StripePaymentService] Checkout session with ID: {} created for booking with ID: {}",
                session.getId(), booking.getId());

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .checkoutId(session.getId())
                .checkoutUrl(session.getUrl())
                .build();
    }

    @Override
    public String createRefund(Booking booking, Long userId) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(booking.getPaymentIntentId())
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata(BOOKING_ID_KEY, String.valueOf(booking.getId()))
                .putMetadata(USER_ID_KEY, String.valueOf(userId))
                .build();

        Refund refund = Refund.create(params);
        log.info("[StripePaymentService] Refund with ID: {} created for booking with ID: {} and paymentIntentId: {}",
                refund.getId(), booking.getId(), booking.getPaymentIntentId());

        return refund.getId();
    }

    @Override
    public PaymentInfoDto getPaymentInfoBySessionId(String sessionId) throws Exception {
        Session session = Session.retrieve(sessionId);

        PaymentInfoDto paymentInfo = new PaymentInfoDto();

        paymentInfo.setCheckoutId(session.getId());
        paymentInfo.setCheckoutStatus(
                session.getStatus() == null ? CheckoutStatus.PENDING :
                switch (session.getStatus()) {
                    case "complete" -> CheckoutStatus.COMPLETED;
                    case "expired" -> CheckoutStatus.EXPIRED;
                    default -> CheckoutStatus.PENDING;
                }
        );

        // No payment was even tried
        if (session.getPaymentIntent() == null || !paymentInfo.getCheckoutStatus().equals(CheckoutStatus.COMPLETED)) {
            paymentInfo.setPaymentStatus(PaymentStatus.PENDING);
            return paymentInfo;
        }

        PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());

        paymentInfo.setPaymentIntentId(paymentIntent.getId());
        paymentInfo.setPaymentStatus(
                paymentIntent.getStatus() == null ? PaymentStatus.PENDING :
                switch (paymentIntent.getStatus()) {
                    case "succeeded" -> PaymentStatus.SUCCEEDED;
                    case "requires_payment_method", "requires_action" -> PaymentStatus.FAILED;
                    default -> PaymentStatus.PENDING;
                }
        );

        return paymentInfo;
    }

    @Override
    public void expireCheckoutSession(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        session.expire();
    }

    private static LineItem createLineItem(Booking booking, String productName) {
        return LineItem.builder()
                .setQuantity(1L)
                .setPriceData(
                        LineItem.PriceData.builder()
                                .setCurrency("brl")
                                .setUnitAmount(convertToCents(booking.getTotalPrice()))
                                .setProductData(
                                        LineItem.PriceData.ProductData.builder()
                                                .setName(productName)
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private static PaymentIntentData createPaymentIntentData(Long userId, Long bookingId) {
        return PaymentIntentData.builder()
                .putMetadata(BOOKING_ID_KEY, String.valueOf(bookingId))
                .putMetadata(USER_ID_KEY, String.valueOf(userId))
                .build();
    }

    private static Long convertToCents(BigDecimal totalPrice) {
        return totalPrice
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
