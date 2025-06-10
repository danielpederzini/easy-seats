package org.pdzsoftware.moviereservationsystem.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingCreatedEvent;
import org.pdzsoftware.moviereservationsystem.dto.request.BookingRequest;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.pdzsoftware.moviereservationsystem.enums.Language;
import org.pdzsoftware.moviereservationsystem.enums.SeatType;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.GoneException;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.*;
import org.pdzsoftware.moviereservationsystem.service.*;
import org.pdzsoftware.moviereservationsystem.usecase.impl.DefaultCreateBookingUseCase;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.AWAITING_PAYMENT;

@ExtendWith(MockitoExtension.class)
public class DefaultCreateBookingUseCaseTest {
    @Mock
    private BookingService bookingService;
    @Mock
    private UserService userService;
    @Mock
    private SessionService sessionService;
    @Mock
    private SeatService seatService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private DefaultCreateBookingUseCase defaultCreateBooking;

    @Test
    @SneakyThrows
    void execute_withValidData_createsBookingAndPublishesEventAndReturnsBookingResponse() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();

        User user = getMockUser();
        Session session = getMockSession();
        List<Seat> seats = getMockSeats();
        Booking booking = buildMockBooking(user, session, seats);
        BookingResponse response = getMockBookingResponse(booking);

        when(userService.findById(anyLong())).thenReturn(Optional.of(user));
        when(sessionService.findById(anyLong())).thenReturn(Optional.of(session));
        when(sessionService.isExpired(any())).thenReturn(false);
        when(seatService.findByIds(any(), any())).thenReturn(seats);
        when(seatService.areAllAvailableToBook(any(), any(), any())).thenReturn(true);
        when(bookingService.createAndSaveBooking(any(), any(), any())).thenReturn(booking);
        when(paymentService.createCheckout(any(), any(), any(), any(), any())).thenReturn(response);

        // Act
        BookingResponse actualResponse = defaultCreateBooking.execute(userId, bookingRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(response.getBookingId(), actualResponse.getBookingId());
        assertEquals(response.getCheckoutId(), actualResponse.getCheckoutId());
        assertEquals(response.getCheckoutUrl(), actualResponse.getCheckoutUrl());

        verify(userService).findById(eq(userId));
        verify(sessionService).findById(eq(bookingRequest.getSessionId()));
        verify(sessionService).isExpired(eq(session.getStartTime()));
        verify(seatService).findByIds(eq(bookingRequest.getSeatIds()), eq(session.getId()));
        verify(seatService).areAllAvailableToBook(eq(session.getId()), eq(user.getId()), eq(bookingRequest.getSeatIds()));
        verify(bookingService).createAndSaveBooking(eq(user), eq(session), eq(seats));
        verify(paymentService).createCheckout(
                eq(user),
                eq(booking),
                eq(bookingRequest.getSuccessUrl()),
                eq(bookingRequest.getCancelUrl()),
                eq(session.getMovie().getTitle())
        );
        verify(bookingService).saveBooking(eq(booking));
        verify(eventPublisher).publishEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @SneakyThrows
    void execute_withNonExistingUser_throwsNotFoundException() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();

        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> defaultCreateBooking.execute(userId, bookingRequest))
                .isInstanceOf(NotFoundException.class);

        verify(userService).findById(eq(userId));
        verify(bookingService, never()).createAndSaveBooking(any(), any(), any());
        verify(bookingService, never()).saveBooking(any());
        verify(paymentService, never()).createCheckout(any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @SneakyThrows
    void execute_withNonExistingSession_throwsNotFoundException() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();

        when(userService.findById(anyLong())).thenReturn(Optional.of(getMockUser()));
        when(sessionService.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> defaultCreateBooking.execute(userId, bookingRequest))
                .isInstanceOf(NotFoundException.class);

        verify(userService).findById(eq(userId));
        verify(sessionService).findById(eq(bookingRequest.getSessionId()));
        verify(bookingService, never()).createAndSaveBooking(any(), any(), any());
        verify(bookingService, never()).saveBooking(any());
        verify(paymentService, never()).createCheckout(any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @SneakyThrows
    void execute_witExpiredSession_throwsGoneException() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();
        Session session = getMockSession();

        when(userService.findById(anyLong())).thenReturn(Optional.of(getMockUser()));
        when(sessionService.findById(anyLong())).thenReturn(Optional.of(session));
        when(sessionService.isExpired(any())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> defaultCreateBooking.execute(userId, bookingRequest))
                .isInstanceOf(GoneException.class);

        verify(userService).findById(eq(userId));
        verify(sessionService).findById(eq(bookingRequest.getSessionId()));
        verify(sessionService).isExpired(eq(session.getStartTime()));
        verify(bookingService, never()).createAndSaveBooking(any(), any(), any());
        verify(bookingService, never()).saveBooking(any());
        verify(paymentService, never()).createCheckout(any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @SneakyThrows
    void execute_withAtLeastOneNonExistingSeat_throwsNotFoundException() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();
        bookingRequest.setSeatIds(Set.of(1L, 2L, 3L, 4L));

        Session session = getMockSession();
        List<Seat> seats = getMockSeats();

        when(userService.findById(anyLong())).thenReturn(Optional.of(getMockUser()));
        when(sessionService.findById(anyLong())).thenReturn(Optional.of(session));
        when(sessionService.isExpired(any())).thenReturn(false);
        when(seatService.findByIds(any(), any())).thenReturn(seats);

        // Act & Assert
        assertThatThrownBy(() -> defaultCreateBooking.execute(userId, bookingRequest))
                .isInstanceOf(NotFoundException.class);

        verify(userService).findById(eq(userId));
        verify(sessionService).findById(eq(bookingRequest.getSessionId()));
        verify(sessionService).isExpired(eq(session.getStartTime()));
        verify(seatService).findByIds(eq(bookingRequest.getSeatIds()), eq(session.getId()));
        verify(bookingService, never()).createAndSaveBooking(any(), any(), any());
        verify(bookingService, never()).saveBooking(any());
        verify(paymentService, never()).createCheckout(any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @SneakyThrows
    void execute_withAtLeastOneUnavailableSeat_throwsConflictException() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();

        User user = getMockUser();
        Session session = getMockSession();
        List<Seat> seats = getMockSeats();

        when(userService.findById(anyLong())).thenReturn(Optional.of(user));
        when(sessionService.findById(anyLong())).thenReturn(Optional.of(session));
        when(sessionService.isExpired(any())).thenReturn(false);
        when(seatService.findByIds(any(), any())).thenReturn(seats);
        when(seatService.areAllAvailableToBook(any(), any(), any())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> defaultCreateBooking.execute(userId, bookingRequest))
                .isInstanceOf(ConflictException.class);

        verify(userService).findById(eq(userId));
        verify(sessionService).findById(eq(bookingRequest.getSessionId()));
        verify(sessionService).isExpired(eq(session.getStartTime()));
        verify(seatService).findByIds(eq(bookingRequest.getSeatIds()), eq(session.getId()));
        verify(seatService).areAllAvailableToBook(eq(session.getId()), eq(user.getId()), eq(bookingRequest.getSeatIds()));
        verify(bookingService, never()).createAndSaveBooking(any(), any(), any());
        verify(bookingService, never()).saveBooking(any());
        verify(paymentService, never()).createCheckout(any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(BookingCreatedEvent.class));
    }

    @Test
    @SneakyThrows
    void execute_withCheckoutCreationException_throwsInternalErrorException() {
        // Arrange
        Long userId = 1L;
        BookingRequest bookingRequest = getMockBookingRequest();

        User user = getMockUser();
        Session session = getMockSession();
        List<Seat> seats = getMockSeats();
        Booking booking = buildMockBooking(user, session, seats);

        when(userService.findById(anyLong())).thenReturn(Optional.of(user));
        when(sessionService.findById(anyLong())).thenReturn(Optional.of(session));
        when(sessionService.isExpired(any())).thenReturn(false);
        when(seatService.findByIds(any(), any())).thenReturn(seats);
        when(seatService.areAllAvailableToBook(any(), any(), any())).thenReturn(true);
        when(bookingService.createAndSaveBooking(any(), any(), any())).thenReturn(booking);
        when(paymentService.createCheckout(any(), any(), any(), any(), any())).thenThrow(RuntimeException.class);

        // Act & Assert
        assertThatThrownBy(() -> defaultCreateBooking.execute(userId, bookingRequest))
                .isInstanceOf(InternalErrorException.class);

        verify(userService).findById(eq(userId));
        verify(sessionService).findById(eq(bookingRequest.getSessionId()));
        verify(sessionService).isExpired(eq(session.getStartTime()));
        verify(seatService).findByIds(eq(bookingRequest.getSeatIds()), eq(session.getId()));
        verify(seatService).areAllAvailableToBook(eq(session.getId()), eq(user.getId()), eq(bookingRequest.getSeatIds()));
        verify(bookingService).createAndSaveBooking(eq(user), eq(session), eq(seats));
        verify(paymentService).createCheckout(
                eq(user),
                eq(booking),
                eq(bookingRequest.getSuccessUrl()),
                eq(bookingRequest.getCancelUrl()),
                eq(session.getMovie().getTitle())
        );
        verify(eventPublisher, never()).publishEvent(any(BookingCreatedEvent.class));
    }

    private BookingRequest getMockBookingRequest() {
        return new BookingRequest(1L, Set.of(1L, 2L, 3L), "success-url", "cancel-url");
    }

    private Booking buildMockBooking(User user, Session session, List<Seat> seats) {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder()
                .bookingStatus(AWAITING_PAYMENT)
                .createdAt(now)
                .updatedAt(now)
                .user(user)
                .session(session)
                .build();

        List<BookedSeat> bookedSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal seatPrice = switch (seat.getSeatType()) {
                case STANDARD -> session.getStandardSeatPrice();
                case VIP -> session.getVipSeatPrice();
                case PWD -> session.getPwdSeatPrice();
            };

            bookedSeats.add(BookedSeat.builder()
                    .seatPrice(seatPrice)
                    .createdAt(now)
                    .updatedAt(now)
                    .booking(booking)
                    .seat(seat)
                    .build());
        }

        booking.setBookedSeats(bookedSeats);
        booking.setTotalPrice(bookedSeats.stream()
                .map(BookedSeat::getSeatPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP));

        return booking;
    }

    private static BookingResponse getMockBookingResponse(Booking booking) {
        return new BookingResponse(booking.getId(), "checkout-id", "checkout-url");
    }

    private static User getMockUser() {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .id(1L)
                .userName("test")
                .email("TeSt@eXaMpLe.com")
                .passwordHash("091i0dqww$@!#")
                .refreshToken("refresh-token")
                .userRole(UserRole.CUSTOMER)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static Session getMockSession() {
        LocalDateTime now = LocalDateTime.now();

        return Session.builder()
                .id(1L)
                .startTime(now.plusMinutes(10))
                .endTime(now.plusMinutes(120))
                .audioLanguage(Language.ENGLISH)
                .hasSubtitles(true)
                .isThreeD(true)
                .standardSeatPrice(BigDecimal.valueOf(50))
                .vipSeatPrice(BigDecimal.valueOf(75))
                .pwdSeatPrice(BigDecimal.valueOf(25))
                .createdAt(now)
                .updatedAt(now)
                .movie(Movie.builder().title("Test Movie").build())
                .build();
    }

    private static List<Seat> getMockSeats() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(getMockStandardSeat(now), getMockVipSeat(now), getMockPwdSeat(now));
    }

    private static Seat getMockStandardSeat(LocalDateTime now) {
        return Seat.builder()
                .id(1L)
                .seatRow("A")
                .seatNumber(1)
                .seatType(SeatType.STANDARD)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static Seat getMockVipSeat(LocalDateTime now) {
        return Seat.builder()
                .id(2L)
                .seatRow("A")
                .seatNumber(2)
                .seatType(SeatType.VIP)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static Seat getMockPwdSeat(LocalDateTime now) {
        return Seat.builder()
                .id(3L)
                .seatRow("A")
                .seatNumber(3)
                .seatType(SeatType.PWD)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
