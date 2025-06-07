package org.pdzsoftware.moviereservationsystem.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingCreatedEvent;
import org.pdzsoftware.moviereservationsystem.dto.event.BookingStatusUpdatedEvent;
import org.pdzsoftware.moviereservationsystem.dto.event.CacheSeatStatusUpdateEvent;
import org.pdzsoftware.moviereservationsystem.dto.event.ClientDisconnectedEvent;
import org.pdzsoftware.moviereservationsystem.dto.response.SeatUpdateResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.model.Booking;
import org.pdzsoftware.moviereservationsystem.service.SeatService;
import org.pdzsoftware.moviereservationsystem.service.SessionService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.AWAITING_CANCELLATION;
import static org.pdzsoftware.moviereservationsystem.enums.BookingStatus.EXPIRED;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatStatusListener {
    private final SeatService seatService;
    private final SessionService sessionService;

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onBookingCreated(BookingCreatedEvent event) {
        seatService.clearUserCacheLockForSession(event.getUserId(), event.getSessionId());

        event.getBookedSeatIds().forEach(id -> broadcastSeatUpdate(
                event.getSessionId(), new SeatUpdateResponse(id, event.getOriginId(), true)
        ));
    }

    @EventListener
    public void onBookingStatusUpdated(BookingStatusUpdatedEvent event) {
        Booking booking = event.getBooking();

        // Only need to broadcast seat status changes for these two booking statuses
        if (Set.of(AWAITING_CANCELLATION, EXPIRED).contains(booking.getBookingStatus())) {
            Set<Long> seatIds = seatService.findIdsByBookingId(booking.getId());

            Long sessionId = sessionService.findIdByBookingId(booking.getId()).orElseThrow(() -> {
                log.error("[SeatStatusListener] Could not find session ID for booking ID: {}", booking.getId());
                return new NotFoundException("Session ID not found for booking ID");
            });

            seatIds.forEach(id -> broadcastSeatUpdate(
                    sessionId, new SeatUpdateResponse(id, event.getOriginId(), false)
            ));
        }
    }

    @EventListener
    public void onCacheSeatStatusUpdated(CacheSeatStatusUpdateEvent event) {
        broadcastSeatUpdate(
                event.getSessionId(), new SeatUpdateResponse(event.getId(), event.getOriginId(), event.isTaken())
        );
    }

    @EventListener
    public void onClientDisconnected(ClientDisconnectedEvent event) {
        Long userId = event.getUserId();
        Long sessionId = event.getSessionId();

        Set<Long> released = seatService.clearUserCacheLockForSession(userId, sessionId);

        released.forEach(id -> broadcastSeatUpdate(
                sessionId, new SeatUpdateResponse(id, event.getOriginId(), false)
        ));
    }

    private void broadcastSeatUpdate(Long sessionId, SeatUpdateResponse update) {
        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/seats", update);
    }
}
