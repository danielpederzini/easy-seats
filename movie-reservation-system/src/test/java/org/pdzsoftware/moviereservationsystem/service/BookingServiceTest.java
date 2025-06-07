package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.enums.Language;
import org.pdzsoftware.moviereservationsystem.enums.SeatType;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;
import org.pdzsoftware.moviereservationsystem.model.*;
import org.pdzsoftware.moviereservationsystem.repository.BookingRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultBookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.*;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.PAST;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private DefaultBookingService bookingService;

    @Test
    void createAndSaveBooking_always_generatesAndSavesWithCorrectInformation() {
        // Arrange
        User user = getMockUser();
        Session session = getMockSession();
        List<Seat> seats = getMockSeats();
        Booking expectedBooking = buildExpectedBooking(user, session, seats);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // Act
        Booking actualBooking = bookingService.createAndSaveBooking(user, session, seats);

        // Assert
        assertNotNull(actualBooking);
        assertEquals(expectedBooking.getBookingStatus(), actualBooking.getBookingStatus());
        assertEquals(expectedBooking.getUser(), actualBooking.getUser());
        assertEquals(expectedBooking.getSession(), actualBooking.getSession());
        assertEquals(expectedBooking.getBookedSeats().size(), actualBooking.getBookedSeats().size());
        assertEquals(expectedBooking.getTotalPrice(), actualBooking.getTotalPrice());

        for (int i = 0; i < seats.size(); i++) {
            Seat expectedSeat = expectedBooking.getBookedSeats().get(i).getSeat();
            Seat actualSeat = actualBooking.getBookedSeats().get(i).getSeat();
            assertEquals(expectedSeat, actualSeat);
        }

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void saveBooking_always_savesWithoutAltering() {
        // Arrange
        Booking expected = getMockBooking();

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // Act
        Booking actual = bookingService.saveBooking(expected);

        // Assert
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBookingStatus(), actual.getBookingStatus());
        assertEquals(expected.getPaymentIntentId(), actual.getPaymentIntentId());
        assertEquals(expected.getCheckoutId(), actual.getCheckoutId());
        assertEquals(expected.getCheckoutUrl(), actual.getCheckoutUrl());
        assertEquals(expected.getTotalPrice(), actual.getTotalPrice());
        assertEquals(expected.getExpiresAt(), actual.getExpiresAt());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

    @Test
    void findByIdAndUserId_withValidIds_returnsOptionalWithBooking() {
        // Arrange
        Booking expected = getMockBooking();
        when(bookingRepository.findByIdAndUserId(any(Long.class), any(Long.class))).thenReturn(Optional.of(expected));

        // Act
        Optional<Booking> actualOptional = bookingService.findByIdAndUserId(1L,1L);

        // Assert
        assertTrue(actualOptional.isPresent());

        Booking actual = actualOptional.get();
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBookingStatus(), actual.getBookingStatus());
        assertEquals(expected.getPaymentIntentId(), actual.getPaymentIntentId());
        assertEquals(expected.getCheckoutId(), actual.getCheckoutId());
        assertEquals(expected.getCheckoutUrl(), actual.getCheckoutUrl());
        assertEquals(expected.getTotalPrice(), actual.getTotalPrice());
        assertEquals(expected.getExpiresAt(), actual.getExpiresAt());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

    @Test
    void findByIdAndUserId_withInvalidIds_returnsEmptyOptional() {
        // Arrange
        when(bookingRepository.findByIdAndUserId(any(Long.class), any(Long.class))).thenReturn(Optional.empty());

        // Act
        Optional<Booking> optional = bookingService.findByIdAndUserId(1L,1L);

        // Assert
        assertTrue(optional.isEmpty());
    }

    @Test
    void findDetailedByFilters_withValidBookingStatuses_filtersByThem() {
        // Arrange
        List<BookingStatus> statuses = List.of(AWAITING_PAYMENT, PAYMENT_RETRY);
        ArgumentCaptor<List<BookingStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);

        when(bookingRepository.findDetailedByFilters(any(Long.class), statusesCaptor.capture(), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        Page<BookingDetailedResponse> response = bookingService.findDetailedByFilters(1L, statuses, 0);

        // Assert
        assertNotNull(response);
        assertIterableEquals(statuses, statusesCaptor.getValue());
    }

    @Test
    void findDetailedByFilters_withNullBookingStatuses_filtersByAllValidStatuses() {
        // Arrange
        ArgumentCaptor<List<BookingStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);

        when(bookingRepository.findDetailedByFilters(any(Long.class), statusesCaptor.capture(), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        Page<BookingDetailedResponse> response = bookingService.findDetailedByFilters(1L, null, 0);

        // Assert
        assertNotNull(response);
        assertIterableEquals(getValidStatuses(), statusesCaptor.getValue());
    }

    @Test
    void findDetailedByFilters_withEmptyBookingStatuses_filtersByAllValidStatuses() {
        // Arrange
        ArgumentCaptor<List<BookingStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);

        when(bookingRepository.findDetailedByFilters(any(Long.class), statusesCaptor.capture(), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        Page<BookingDetailedResponse> response = bookingService.findDetailedByFilters(1L, List.of(), 0);

        // Assert
        assertNotNull(response);
        assertIterableEquals(getValidStatuses(), statusesCaptor.getValue());
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
                .id(1L)
                .seatRow("A")
                .seatNumber(2)
                .seatType(SeatType.VIP)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static Seat getMockPwdSeat(LocalDateTime now) {
        return Seat.builder()
                .id(1L)
                .seatRow("A")
                .seatNumber(3)
                .seatType(SeatType.PWD)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Booking buildExpectedBooking(User user, Session session, List<Seat> seats) {
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

    private Booking getMockBooking() {
        LocalDateTime now = LocalDateTime.now();

        return Booking.builder()
                .id(1L)
                .bookingStatus(PAYMENT_CONFIRMED)
                .totalPrice(BigDecimal.valueOf(100))
                .paymentIntentId("payment-intent-id")
                .checkoutId("checkout-id")
                .checkoutUrl("checkout-url")
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(5))
                .build();
    }

    private List<BookingStatus> getValidStatuses() {
        return List.of(
                AWAITING_PAYMENT,
                PAYMENT_RETRY,
                PAYMENT_CONFIRMED,
                AWAITING_CANCELLATION,
                CANCELLED,
                PAST
        );
    }
}
