package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingStatusUpdatedEvent;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.repository.BookingRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultBookingStatusService;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.*;

@ExtendWith(MockitoExtension.class)
class DefaultBookingStatusServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private DefaultBookingStatusService bookingService;

    @Test
    void validateAndUpdateStatus_withValidStatusChange_updatesStatusAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.validateAndUpdateStatus(booking, AWAITING_CANCELLATION);

        // Assert
        assertEquals(AWAITING_CANCELLATION, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void validateAndUpdateStatus_withInvalidStatusChange_throwsConflictException() {
        // Arrange
        Booking booking = getMockBooking();

        // Act & Assert
        assertThatThrownBy(() -> bookingService.validateAndUpdateStatus(booking, AWAITING_PAYMENT))
                .isInstanceOf(ConflictException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handleCheckoutCompleted_withOutdatedBooking_updatesAndSavesPaymentIntentId() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setPaymentIntentId(null);

        String checkoutId = "checkout-id";
        String paymentIntentId = "payment-intent-id";

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handleCheckoutCompleted(booking, checkoutId, paymentIntentId);

        // Assert
        assertEquals(paymentIntentId, bookingCaptor.getValue().getPaymentIntentId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void handleCheckoutCompleted_withOutdatedBookingWithInvalidCheckoutId_throwsNotFoundException() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setPaymentIntentId(null);

        String checkoutId = "checkout-id2";
        String paymentIntentId = "payment-intent-id";

        // Act & Assert
        assertThatThrownBy(() -> bookingService.handleCheckoutCompleted(booking, checkoutId, paymentIntentId))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void handleCheckoutCompleted_withUpdatedBooking_doesNothing() {
        // Arrange
        Booking booking = getMockBooking();

        String checkoutId = "checkout-id";
        String paymentIntentId = "payment-intent-id";

        // Act
        bookingService.handleCheckoutCompleted(booking, checkoutId, paymentIntentId);

        // Assert
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void handleCheckoutExpired_withOutdatedBooking_updatesAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String checkoutId = "checkout-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handleCheckoutExpired(booking, checkoutId);

        // Assert
        assertEquals(EXPIRED, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handleCheckoutExpired_withOutdatedBookingWithInvalidCheckoutId_throwsNotFoundException() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String checkoutId = "checkout-id2";

        // Act & Assert
        assertThatThrownBy(() -> bookingService.handleCheckoutExpired(booking, checkoutId))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handleCheckoutExpired_withUpdatedBooking_doesNothing() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(EXPIRED);

        String checkoutId = "checkout-id";

        // Act
        bookingService.handleCheckoutExpired(booking, checkoutId);

        // Assert
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentSuccess_withOutdatedBooking_updatesAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setPaymentIntentId(null);
        booking.setBookingStatus(AWAITING_PAYMENT);

        String paymentIntentId = "payment-intent-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handlePaymentSuccess(booking, paymentIntentId);

        // Assert
        assertEquals(paymentIntentId, bookingCaptor.getValue().getPaymentIntentId());
        assertEquals(PAYMENT_CONFIRMED, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentSuccess_withPartiallyOutdatedBooking_updatesAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String paymentIntentId = "payment-intent-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handlePaymentSuccess(booking, paymentIntentId);

        // Assert
        assertEquals(paymentIntentId, bookingCaptor.getValue().getPaymentIntentId());
        assertEquals(PAYMENT_CONFIRMED, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentSuccess_withOutdatedBookingWithInvalidPaymentIntentId_throwsNotFoundException() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String paymentIntentId = "payment-intent-id2";

        // Act & Assert
        assertThatThrownBy(() -> bookingService.handlePaymentSuccess(booking, paymentIntentId))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentSuccess_withUpdatedBooking_doesNothing() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(PAYMENT_CONFIRMED);

        String paymentIntentId = "payment-intent-id";

        // Act
        bookingService.handlePaymentSuccess(booking, paymentIntentId);

        // Assert
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentFailed_withOutdatedBooking_updatesAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setPaymentIntentId(null);
        booking.setBookingStatus(AWAITING_PAYMENT);

        String paymentIntentId = "payment-intent-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handlePaymentFailed(booking, paymentIntentId);

        // Assert
        assertEquals(paymentIntentId, bookingCaptor.getValue().getPaymentIntentId());
        assertEquals(PAYMENT_RETRY, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentFailed_withPartiallyOutdatedBooking_updatesAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String paymentIntentId = "payment-intent-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handlePaymentFailed(booking, paymentIntentId);

        // Assert
        assertEquals(paymentIntentId, bookingCaptor.getValue().getPaymentIntentId());
        assertEquals(PAYMENT_RETRY, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentFailed_withOutdatedBookingWithInvalidPaymentIntentId_throwsNotFoundException() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String paymentIntentId = "payment-intent-id2";

        // Act & Assert
        assertThatThrownBy(() -> bookingService.handlePaymentFailed(booking, paymentIntentId))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentFailed_withUpdatedBooking_updatesUpdatedAtFieldAndSaves() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(PAYMENT_RETRY);

        LocalDateTime originalUpdatedAt = booking.getUpdatedAt();

        String paymentIntentId = "payment-intent-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handlePaymentFailed(booking, paymentIntentId);

        // Assert
        LocalDateTime capturedUpdatedAt = bookingCaptor.getValue().getUpdatedAt();

        assertNotNull(capturedUpdatedAt);
        assertTrue(capturedUpdatedAt.isAfter(originalUpdatedAt));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentRefunded_withOutdatedBooking_updatesAndPublishesEvent() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_CANCELLATION);

        String refundId = "refund-id";
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        // Act
        bookingService.handlePaymentRefunded(booking, refundId);

        // Assert
        assertEquals(CANCELLED, bookingCaptor.getValue().getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
        verify(eventPublisher).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentRefunded_withOutdatedBookingWithInvalidRefundId_throwsNotFoundException() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(AWAITING_PAYMENT);

        String refundId = "refund-id2";

        // Act & Assert
        assertThatThrownBy(() -> bookingService.handlePaymentRefunded(booking, refundId))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    @Test
    void handlePaymentRefunded_withUpdatedBooking_doesNothing() {
        // Arrange
        Booking booking = getMockBooking();
        booking.setBookingStatus(CANCELLED);

        String refundId = "refund-id";

        // Act
        bookingService.handlePaymentRefunded(booking, refundId);

        // Assert
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(eventPublisher, never()).publishEvent(any(BookingStatusUpdatedEvent.class));
    }

    private Booking getMockBooking() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);

        return Booking.builder()
                .id(1L)
                .bookingStatus(PAYMENT_CONFIRMED)
                .totalPrice(BigDecimal.valueOf(100))
                .paymentIntentId("payment-intent-id")
                .checkoutId("checkout-id")
                .refundId("refund-id")
                .checkoutUrl("checkout-url")
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(5))
                .build();
    }
}
