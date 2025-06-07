package org.pdzsoftware.moviereservationsystem.dto.event;

import lombok.*;
import org.pdzsoftware.moviereservationsystem.model.Booking;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusUpdatedEvent {
    private Booking booking;
    private String originId;
}
