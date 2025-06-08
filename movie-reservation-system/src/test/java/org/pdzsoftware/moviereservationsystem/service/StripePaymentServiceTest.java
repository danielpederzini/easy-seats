package org.pdzsoftware.moviereservationsystem.service;

import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.dto.PaymentInfoDto;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.pdzsoftware.moviereservationsystem.enums.CheckoutStatus;
import org.pdzsoftware.moviereservationsystem.enums.PaymentStatus;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;
import org.pdzsoftware.moviereservationsystem.model.BookedSeat;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.service.impl.StripePaymentService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stripe.param.checkout.SessionCreateParams.*;
import static com.stripe.param.checkout.SessionCreateParams.LineItem.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.PAYMENT_CONFIRMED;
import static org.pdzsoftware.moviereservationsystem.service.PaymentService.BOOKING_ID_KEY;
import static org.pdzsoftware.moviereservationsystem.service.PaymentService.USER_ID_KEY;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {
    @Mock
    private SessionCreateParams sessionCreateParams;
    @Mock
    private RefundCreateParams refundCreateParams;
    @InjectMocks
    private StripePaymentService paymentService;

    @Test
    @SneakyThrows
    void createCheckout_always_createsCheckoutSessionWithCorrectInformationAndReturnsBookingResponse() {
        // Arrange
        User user = getMockUser();
        Booking booking = getMockBooking();
        String successUrl = "https://example.com/success";
        String cancelUrl = "https://example.com/cancel";
        String movieTitle = "Example Movie";
        Session mockSession = getMockSession();

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            ArgumentCaptor<SessionCreateParams> sessionParamsCaptor = ArgumentCaptor.forClass(SessionCreateParams.class);

            // Act
            BookingResponse response = paymentService.createCheckout(user, booking, successUrl, cancelUrl, movieTitle);

            // Assert
            mockedSession.verify(() -> Session.create(sessionParamsCaptor.capture()));

            SessionCreateParams sessionParams = sessionParamsCaptor.getValue();
            assertEquals(Mode.PAYMENT, sessionParams.getMode());
            assertEquals(buildSuccessUrl(successUrl, booking.getId()), sessionParams.getSuccessUrl());
            assertEquals(cancelUrl, sessionParams.getCancelUrl());
            assertEquals(user.getEmail(), sessionParams.getCustomerEmail());
            assertEquals(booking.getId().toString(), sessionParams.getMetadata().get(BOOKING_ID_KEY));
            assertEquals(user.getId().toString(), sessionParams.getMetadata().get(USER_ID_KEY));
            assertEquals(1, sessionParams.getLineItems().size());

            LineItem lineItem = sessionParams.getLineItems().get(0);
            assertEquals(1, lineItem.getQuantity());

            PriceData priceData = lineItem.getPriceData();
            assertEquals("brl", priceData.getCurrency());
            assertEquals(convertToCents(booking.getTotalPrice()), priceData.getUnitAmount());
            assertEquals(buildProductName(booking, movieTitle), priceData.getProductData().getName());

            PaymentIntentData paymentIntentData = sessionParams.getPaymentIntentData();
            assertEquals(booking.getId().toString(), paymentIntentData.getMetadata().get(BOOKING_ID_KEY));
            assertEquals(user.getId().toString(), paymentIntentData.getMetadata().get(USER_ID_KEY));

            assertEquals(response.getBookingId(), booking.getId());
            assertEquals(response.getCheckoutId(), mockSession.getId());
            assertEquals(response.getCheckoutUrl(), mockSession.getUrl());
        }
    }

    @Test
    @SneakyThrows
    void createRefund_always_createsRefundWithCorrectInformationAndReturnsRefundId() {
        // Arrange
        User user = getMockUser();
        Booking booking = getMockBooking();
        booking.setPaymentIntentId("payment-intent-id");
        Refund mockRefund = getMockRefund();

        try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
            mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class)))
                    .thenReturn(mockRefund);

            ArgumentCaptor<RefundCreateParams> refundParamsCaptor = ArgumentCaptor.forClass(RefundCreateParams.class);

            // Act
            String refundId = paymentService.createRefund(booking, user.getId());

            // Assert
            mockedRefund.verify(() -> Refund.create(refundParamsCaptor.capture()));

            RefundCreateParams refundParams = refundParamsCaptor.getValue();
            assertEquals(booking.getPaymentIntentId(), refundParams.getPaymentIntent());
            assertEquals(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER, refundParams.getReason());

            Map<String, String> metadata = (HashMap<String, String>) refundParams.getMetadata();
            assertEquals(booking.getId().toString(), metadata.get(BOOKING_ID_KEY));
            assertEquals(user.getId().toString(), metadata.get(USER_ID_KEY));

            assertEquals(refundId, mockRefund.getId());
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withSuccessfulCheckout_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("complete");
        mockSession.setPaymentIntent("payment-intent-id");

        PaymentIntent mockPaymentIntent = getMockPaymentIntent();
        mockPaymentIntent.setStatus("succeeded");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                        .thenReturn(mockSession);
                mockedPaymentIntent.when(() -> PaymentIntent.retrieve(eq(mockSession.getPaymentIntent())))
                        .thenReturn(mockPaymentIntent);

                // Act
                PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

                // Assert
                mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

                assertNotNull(response);
                assertEquals(mockSession.getId(), response.getCheckoutId());
                assertEquals(mockSession.getPaymentIntent(), response.getPaymentIntentId());
                assertEquals(mockPaymentIntent.getId(), response.getPaymentIntentId());
                assertEquals(CheckoutStatus.COMPLETED, response.getCheckoutStatus());
                assertEquals(PaymentStatus.SUCCEEDED, response.getPaymentStatus());
            }
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withFailedCheckout_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("complete");
        mockSession.setPaymentIntent("payment-intent-id");

        PaymentIntent mockPaymentIntent = getMockPaymentIntent();
        mockPaymentIntent.setStatus("requires_action");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                        .thenReturn(mockSession);
                mockedPaymentIntent.when(() -> PaymentIntent.retrieve(eq(mockSession.getPaymentIntent())))
                        .thenReturn(mockPaymentIntent);

                // Act
                PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

                // Assert
                mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

                assertNotNull(response);
                assertEquals(mockSession.getId(), response.getCheckoutId());
                assertEquals(mockSession.getPaymentIntent(), response.getPaymentIntentId());
                assertEquals(mockPaymentIntent.getId(), response.getPaymentIntentId());
                assertEquals(CheckoutStatus.COMPLETED, response.getCheckoutStatus());
                assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
            }
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withExpiredCheckout_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("expired");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                    .thenReturn(mockSession);

            // Act
            PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

            // Assert
            mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

            assertNotNull(response);
            assertEquals(mockSession.getId(), response.getCheckoutId());
            assertEquals(CheckoutStatus.EXPIRED, response.getCheckoutStatus());
            assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withNullPaymentIntentId_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                    .thenReturn(mockSession);

            // Act
            PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

            // Assert
            mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

            assertNotNull(response);
            assertNull(response.getPaymentIntentId());
            assertEquals(mockSession.getId(), response.getCheckoutId());
            assertEquals(mockSession.getPaymentIntent(), response.getPaymentIntentId());
            assertEquals(CheckoutStatus.PENDING, response.getCheckoutStatus());
            assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withExpiredCheckoutWithPaymentIntentId_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("expired");
        // This should never happen as expired sessions don't have payment intent ids
        mockSession.setPaymentIntent("payment-intent-id");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                    .thenReturn(mockSession);

            // Act
            PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

            // Assert
            mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

            assertNotNull(response);
            assertNull(response.getPaymentIntentId());
            assertEquals(mockSession.getId(), response.getCheckoutId());
            assertEquals(CheckoutStatus.EXPIRED, response.getCheckoutStatus());
            assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withUnknownCheckoutStatus_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("unknown");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                    .thenReturn(mockSession);

            // Act
            PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

            // Assert
            mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

            assertNotNull(response);
            assertNull(response.getPaymentIntentId());
            assertEquals(mockSession.getId(), response.getCheckoutId());
            assertEquals(CheckoutStatus.PENDING, response.getCheckoutStatus());
            assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withUnknownPaymentStatus_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("complete");
        mockSession.setPaymentIntent("payment-intent-id");

        PaymentIntent mockPaymentIntent = getMockPaymentIntent();
        mockPaymentIntent.setStatus("unknown");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                        .thenReturn(mockSession);
                mockedPaymentIntent.when(() -> PaymentIntent.retrieve(eq(mockSession.getPaymentIntent())))
                        .thenReturn(mockPaymentIntent);

                // Act
                PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

                // Assert
                mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

                assertNotNull(response);
                assertEquals(mockSession.getId(), response.getCheckoutId());
                assertEquals(mockSession.getPaymentIntent(), response.getPaymentIntentId());
                assertEquals(mockPaymentIntent.getId(), response.getPaymentIntentId());
                assertEquals(CheckoutStatus.COMPLETED, response.getCheckoutStatus());
                assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
            }
        }
    }

    @Test
    @SneakyThrows
    void getPaymentInfoBySessionId_withNullPaymentStatus_returnsPaymentInfoDtoWithCorrectInformation() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = getMockSession();
        mockSession.setStatus("complete");
        mockSession.setPaymentIntent("payment-intent-id");

        PaymentIntent mockPaymentIntent = getMockPaymentIntent();

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                        .thenReturn(mockSession);
                mockedPaymentIntent.when(() -> PaymentIntent.retrieve(eq(mockSession.getPaymentIntent())))
                        .thenReturn(mockPaymentIntent);

                // Act
                PaymentInfoDto response = paymentService.getPaymentInfoBySessionId(sessionId);

                // Assert
                mockedSession.verify(() -> Session.retrieve(eq(sessionId)));

                assertNotNull(response);
                assertEquals(mockSession.getId(), response.getCheckoutId());
                assertEquals(mockSession.getPaymentIntent(), response.getPaymentIntentId());
                assertEquals(mockPaymentIntent.getId(), response.getPaymentIntentId());
                assertEquals(CheckoutStatus.COMPLETED, response.getCheckoutStatus());
                assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
            }
        }
    }

    @Test
    @SneakyThrows
    void expireCheckoutSession_always_retrievesSessionAndCallsExpire() {
        // Arrange
        String sessionId = "checkout-id";

        Session mockSession = mock(Session.class);
        mockSession.setStatus("complete");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(eq(sessionId)))
                    .thenReturn(mockSession);

            when(mockSession.expire()).thenReturn(mockSession);

            // Act
            paymentService.expireCheckoutSession(sessionId);

            // Assert
            mockedSession.verify(() -> Session.retrieve(eq(sessionId)));
            verify(mockSession).expire();
        }
    }

    private static Session getMockSession() {
        Session session = new Session();
        session.setId("checkout-id");
        session.setUrl("https://example.com/checkout");

        return session;
    }

    private static PaymentIntent getMockPaymentIntent() {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("payment-intent-id");

        return paymentIntent;
    }

    private static Refund getMockRefund() {
        Refund refund = new Refund();
        refund.setId("refund-id");

        return refund;
    }

    private static User getMockUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("oqwdk012d1$$@!ds")
                .userRole(UserRole.CUSTOMER)
                .build();
    }

    private Booking getMockBooking() {
        LocalDateTime now = LocalDateTime.now();

        BookedSeat bookedSeat = BookedSeat.builder()
                .id(1L)
                .seatPrice(BigDecimal.valueOf(50))
                .createdAt(now)
                .updatedAt(now)
                .build();


        return Booking.builder()
                .id(1L)
                .bookingStatus(PAYMENT_CONFIRMED)
                .totalPrice(BigDecimal.valueOf(100))
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(5))
                .bookedSeats(List.of(bookedSeat))
                .build();
    }

    private String buildSuccessUrl(String successUrl, Long bookingId) {
        return successUrl + "?bookingId=" + bookingId + "&checkoutId={CHECKOUT_SESSION_ID}";
    }

    private static Long convertToCents(BigDecimal totalPrice) {
        return totalPrice
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private static String buildProductName(Booking booking, String movieTitle) {
        return String.format("%s movie ticket(s) for %s", booking.getBookedSeats().size(), movieTitle);
    }
}
