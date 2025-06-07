package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingDetailedResponse {
    private Long id;
    private BookingStatus bookingStatus;
    private BigDecimal totalPrice;
    private String checkoutId;
    private String checkoutUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private boolean checkoutCompleted;

    private MovieResponse movie;
    private SessionResponse session;
    private List<BookedSeatResponse> bookedSeats;

    public BookingDetailedResponse(Long id,
                                   BookingStatus bookingStatus,
                                   BigDecimal totalPrice,
                                   String checkoutId,
                                   String checkoutUrl,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt,
                                   LocalDateTime expiresAt,
                                   String paymentIntentId) {
        this.id = id;
        this.bookingStatus = bookingStatus;
        this.totalPrice = totalPrice;
        this.checkoutId = checkoutId;
        this.checkoutUrl = checkoutUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.checkoutCompleted = paymentIntentId != null;
    }
}
