package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String checkoutId;
    private String checkoutURL;
}