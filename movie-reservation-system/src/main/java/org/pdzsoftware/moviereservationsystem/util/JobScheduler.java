package org.pdzsoftware.moviereservationsystem.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {
    private final BookingJobRunner bookingJobRunner;
    private final TaskScheduler taskScheduler;

    public void scheduleBookingExpiration(Long bookingId, LocalDateTime runAt) {
        taskScheduler.schedule(() -> bookingJobRunner.tryExpiringBooking(bookingId),
                runAt.atZone(ZoneId.systemDefault()).toInstant());

        log.info("[JobScheduler] Scheduled expiration for booking with ID: {}, to run at {}",
                bookingId, runAt);
    }
}
