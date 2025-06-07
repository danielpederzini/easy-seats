package org.pdzsoftware.moviereservationsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.request.BookingRequest;
import org.pdzsoftware.moviereservationsystem.dto.request.CheckoutInfoRequest;
import org.pdzsoftware.moviereservationsystem.dto.request.TokenRequest;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.pdzsoftware.moviereservationsystem.usecase.*;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final CreateBookingUseCase createBooking;
    private final CreateQrCodeUseCase createQrCode;
    private final ValidateQrCodeUseCase validateQrCode;
    private final TryConfirmingPaymentUseCase tryConfirmingPayment;
    private final GetUserBookingsUseCase getUserBookings;
    private final CancelBookingUseCase cancelBooking;

    private final JwtUtils jwtUtils;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(@CookieValue("accessToken") String accessToken,
                                                         @RequestBody @Valid BookingRequest bookingRequest) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(createBooking.execute(userId, bookingRequest));
    }

    @PostMapping("/{id}/qr-code")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> createQrCode(@CookieValue("accessToken") String accessToken,
                                               @PathVariable Long id) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);

        byte[] qrCode = createQrCode.execute(id, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCode.length);

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(qrCode);
    }

    @PatchMapping("/validate-qr-code")
    public ResponseEntity<Void> validateQrCode(@RequestBody @Valid TokenRequest qrCode) {
        validateQrCode.execute(qrCode.getToken());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/try-confirming")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Boolean> tryConfirmingPayment(@CookieValue("accessToken") String accessToken,
                                                        @RequestBody @Valid CheckoutInfoRequest checkoutInfoRequest,
                                                        @PathVariable Long id) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(tryConfirmingPayment.execute(userId, id, checkoutInfoRequest));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<BookingDetailedResponse>> getUserBookings(@CookieValue("accessToken") String accessToken,
                                                                         @RequestParam(required = false) List<BookingStatus> statuses,
                                                                         @RequestParam(defaultValue = "0") int page) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(getUserBookings.execute(userId, statuses, page));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> cancelBooking(@CookieValue("accessToken") String accessToken,
                                              @PathVariable Long id) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        cancelBooking.execute(userId, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
