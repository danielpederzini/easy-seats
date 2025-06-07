package org.pdzsoftware.moviereservationsystem.usecase.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.BookingService;
import org.pdzsoftware.moviereservationsystem.service.UserService;
import org.pdzsoftware.moviereservationsystem.usecase.CreateQrCodeUseCase;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.PAYMENT_CONFIRMED;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultCreateQrCodeUseCase implements CreateQrCodeUseCase {
    private final BookingService bookingService;
    private final UserService userService;

    private final JwtUtils jwtUtils;

    @Override
    public byte[] execute(Long bookingId, Long userId) {
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User not found for given ID");
        }

        Booking booking = bookingService.findByIdAndUserId(bookingId, userId).orElseThrow(() -> {
            log.error("[DefaultCreateQrCodeUseCase] Booking not found for ID: {} and user ID: {}", bookingId, userId);
            return new NotFoundException("Booking not found for given ID and user ID");
        });

        if (booking.getBookingStatus() != PAYMENT_CONFIRMED) {
            log.error("[DefaultCreateQrCodeUseCase] Tried creating QRCode for booking with invalid status: {}, booking ID: {}",
                    booking.getBookingStatus(), bookingId);
            throw new ConflictException("Booking is not in PAYMENT_CONFIRMED status");
        }

        String text = jwtUtils.generateQrCode(bookingId, userId);
        return tryGeneratingImage(text);
    }

    private static byte[] tryGeneratingImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("[DefaultCreateQrCodeUseCase] Error creating QRCode", e);
            throw new InternalErrorException("Internal error creating QRCode");
        }
    }
}
