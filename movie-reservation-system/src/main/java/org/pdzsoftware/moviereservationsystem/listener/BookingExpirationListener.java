package org.pdzsoftware.moviereservationsystem.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingCreatedEvent;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.util.JobScheduler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingExpirationListener {
    private static final long GRACE_PERIOD_MS = 5 * 1000L;

    private final JobScheduler jobScheduler;

    @EventListener
    public void onBookingCreated(BookingCreatedEvent event) {
        Booking booking = event.getBooking();

        jobScheduler.scheduleBookingExpiration(
                booking.getId(), booking.getExpiresAt().plus(GRACE_PERIOD_MS, ChronoUnit.MILLIS)
        );
    }
}
