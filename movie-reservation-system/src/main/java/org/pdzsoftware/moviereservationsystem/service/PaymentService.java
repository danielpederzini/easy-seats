package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.PaymentInfoDto;
import org.pdzsoftware.moviereservationsystem.dto.response.BookingResponse;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    String BOOKING_ID_KEY = "bookingId";
    String USER_ID_KEY = "userId";

    BookingResponse createCheckout(User user, Booking booking, String successUrl, String cancelUrl, String movieTitle) throws Exception;
    String createRefund(Booking booking, Long userId) throws Exception;
    PaymentInfoDto getPaymentInfoBySessionId(String sessionId) throws Exception;
    void expireCheckoutSession(String sessionId) throws Exception;
}
