package org.pdzsoftware.moviereservationsystem.dto.event;

import lombok.*;
import org.pdzsoftware.moviereservationsystem.model.Booking;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreatedEvent {
    private Long userId;
    private Long sessionId;
    private Booking booking;
    private String originId;
    private Set<Long> bookedSeatIds;
}
