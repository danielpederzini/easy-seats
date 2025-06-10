package org.pdzsoftware.moviereservationsystem.service;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.BookedSeat;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.impl.StripeWebhookService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.PAYMENT_CONFIRMED;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {
    @Mock
    private BookingService bookingService;
    @Mock
    private BookingStatusService bookingStatusService;
    @InjectMocks
    private StripeWebhookService webhookService;

    @Test
    @SneakyThrows
    void handleStripeEvent_caseSessionCompleted_retrievesSessionAndBookingAndCallsStatusService() {
        // Arrange
        Event event = getMockEvent();
        Session session = getMockSession();
        Booking booking = getMockBooking();

        when(event.getType()).thenReturn("checkout.session.completed");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(session);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(booking));

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService).handleCheckoutCompleted(
                booking, session.getId(), session.getPaymentIntent()
        );
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseSessionCompletedWithNoBookingFound_throwsNotFoundException() {
        // Arrange
        Event event = getMockEvent();
        Session session = getMockSession();

        when(event.getType()).thenReturn("checkout.session.completed");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(session);

        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> webhookService.handleStripeEvent(event))
                .isInstanceOf(NotFoundException.class);

        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService, never()).handleCheckoutCompleted(any(), any(), any());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_casePaymentSucceeded_retrievesPaymentIntentAndBookingAndCallsStatusService() {
        // Arrange
        Event event = getMockEvent();
        PaymentIntent paymentIntent = getMockPaymentIntent();
        Booking booking = getMockBooking();

        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(paymentIntent);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(booking));

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService).handlePaymentSuccess(booking, paymentIntent.getId());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_casePaymentSucceededWithNoBookingFound_throwsNotFoundException() {
        // Arrange
        Event event = getMockEvent();
        PaymentIntent paymentIntent = getMockPaymentIntent();

        when(event.getType()).thenReturn("payment_intent.succeeded");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(paymentIntent);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> webhookService.handleStripeEvent(event))
                .isInstanceOf(NotFoundException.class);

        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService, never()).handlePaymentSuccess(any(), any());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseSessionExpired_retrievesSessionAndBookingAndCallsStatusService() {
        // Arrange
        Event event = getMockEvent();
        Session session = getMockSession();
        Booking booking = getMockBooking();

        when(event.getType()).thenReturn("checkout.session.expired");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(session);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(booking));

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService).handleCheckoutExpired(booking, session.getId());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseSessionExpiredWithNoBookingFound_throwsNotFoundException() {
        // Arrange
        Event event = getMockEvent();
        Session session = getMockSession();

        when(event.getType()).thenReturn("checkout.session.expired");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(session);

        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> webhookService.handleStripeEvent(event))
                .isInstanceOf(NotFoundException.class);

        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService, never()).handleCheckoutExpired(any(), any());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_casePaymentFailed_retrievesPaymentIntentAndBookingAndCallsStatusService() {
        // Arrange
        Event event = getMockEvent();
        PaymentIntent paymentIntent = getMockPaymentIntent();
        Booking booking = getMockBooking();

        when(event.getType()).thenReturn("payment_intent.payment_failed");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(paymentIntent);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(booking));

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService).handlePaymentFailed(booking, paymentIntent.getId());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_casePaymentFailedWithNoBookingFound_throwsNotFoundException() {
        // Arrange
        Event event = getMockEvent();
        PaymentIntent paymentIntent = getMockPaymentIntent();

        when(event.getType()).thenReturn("payment_intent.payment_failed");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(paymentIntent);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> webhookService.handleStripeEvent(event))
                .isInstanceOf(NotFoundException.class);

        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService, never()).handlePaymentFailed(any(), any());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseRefundSucceeded_retrievesPaymentIntentAndBookingAndCallsStatusService() {
        // Arrange
        Event event = getMockEvent();
        Refund refund = getMockRefund();
        Booking booking = getMockBooking();

        when(event.getType()).thenReturn("refund.updated");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(refund);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(booking));

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService).handlePaymentRefunded(booking, refund.getId());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseRefundSucceededWithNoBookingFound_throwsNotFoundException() {
        // Arrange
        Event event = getMockEvent();
        Refund refund = getMockRefund();

        when(event.getType()).thenReturn("refund.updated");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(refund);
        when(bookingService.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> webhookService.handleStripeEvent(event))
                .isInstanceOf(NotFoundException.class);

        verify(bookingService).findByIdAndUserId(1L, 1L);
        verify(bookingStatusService, never()).handlePaymentRefunded(any(), any());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseRefundNotSucceeded_ignore() {
        // Arrange
        Event event = getMockEvent();
        Refund refund = getMockRefund();
        refund.setStatus("unknown");

        when(event.getType()).thenReturn("refund.updated");
        when(event.getData()).thenReturn(mock(Event.Data.class));
        when(event.getData().getObject()).thenReturn(refund);

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingStatusService, never()).handlePaymentRefunded(any(), any());
    }

    @Test
    @SneakyThrows
    void handleStripeEvent_caseEventTypeUnknown_ignore() {
        // Arrange
        Event event = getMockEvent();
        when(event.getType()).thenReturn("unknown.event");

        // Act
        webhookService.handleStripeEvent(event);

        // Assert
        verify(bookingStatusService, never()).handleCheckoutCompleted(any(), any(), any());
        verify(bookingStatusService, never()).handlePaymentSuccess(any(), any());
        verify(bookingStatusService, never()).handleCheckoutExpired(any(), any());
        verify(bookingStatusService, never()).handlePaymentFailed(any(), any());
        verify(bookingStatusService, never()).handlePaymentRefunded(any(), any());
    }

    private static Event getMockEvent() {
        return mock(Event.class);
    }

    private static Session getMockSession() {
        Session session = new Session();
        session.setId("checkout-id");
        session.setPaymentIntent("payment-intent-id");
        session.setUrl("https://example.com/checkout");
        session.setMetadata(Map.of("bookingId", "1", "userId", "1"));

        return session;
    }

    private static PaymentIntent getMockPaymentIntent() {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("payment-intent-id");
        paymentIntent.setMetadata(Map.of("bookingId", "1", "userId", "1"));

        return paymentIntent;
    }

    private static Refund getMockRefund() {
        Refund refund = new Refund();
        refund.setId("refund-id");
        refund.setStatus("succeeded");
        refund.setMetadata(Map.of("bookingId", "1", "userId", "1"));

        return refund;
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
}
