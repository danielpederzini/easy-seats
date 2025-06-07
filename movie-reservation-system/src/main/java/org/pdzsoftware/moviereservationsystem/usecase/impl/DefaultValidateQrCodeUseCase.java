package org.pdzsoftware.moviereservationsystem.usecase.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.exception.custom.UnauthorizedException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.pdzsoftware.moviereservationsystem.service.UserService;
import org.pdzsoftware.moviereservationsystem.usecase.ValidateQrCodeUseCase;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.PAYMENT_CONFIRMED;
import static org.pdzsoftware.moviereservationsystem.util.JwtUtils.BOOKING_ID;
import static org.pdzsoftware.moviereservationsystem.util.JwtUtils.USER_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultValidateQrCodeUseCase implements ValidateQrCodeUseCase {
    private final BookingService bookingService;
    private final UserService userService;

    private final JwtUtils jwtUtils;

    @Override
    public void execute(String qrCode) {
        if (!jwtUtils.isQrCodeValid(qrCode)) {
            throw new UnauthorizedException("Invalid QRCode JWT");
        }

        Claims claims = jwtUtils.getAllClaimsFromToken(qrCode);

        Long codeUserId = claims.get(USER_ID, Long.class);
        Long codeBookingId = claims.get(BOOKING_ID, Long.class);

        if (!userService.existsById(codeUserId)) {
            throw new NotFoundException("User not found for given ID");
        }

        Booking booking = bookingService.findByIdAndUserId(codeBookingId, codeUserId).orElseThrow(() -> {
            log.error("[DefaultValidateQrCodeUseCase] Booking not found for ID: {} and user ID: {}", codeBookingId, codeUserId);
            return new NotFoundException("Booking not found for given ID and user ID");
        });

        if (booking.getBookingStatus() != PAYMENT_CONFIRMED) {
            throw new ConflictException("Booking is not in PAYMENT_CONFIRMED status");
        }

        if (booking.isQrCodeValidated()) {
            throw new ConflictException("QRCode has already been validated");
        }

        booking.setQrCodeValidated(true);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingService.saveBooking(booking);
    }
}
