package org.pdzsoftware.moviereservationsystem.enums;

import java.util.Set;

public enum BookingStatus {
    AWAITING_DELETION(Set.of()),
    CANCELLED(Set.of()),
    PAST(Set.of()),
    AWAITING_CANCELLATION(Set.of(CANCELLED)),
    EXPIRED(Set.of(AWAITING_CANCELLATION, AWAITING_DELETION)),
    PAYMENT_CONFIRMED(Set.of(AWAITING_CANCELLATION, PAST)),
    PAYMENT_RETRY(Set.of(PAYMENT_CONFIRMED, EXPIRED)),
    AWAITING_PAYMENT(Set.of(PAYMENT_CONFIRMED, PAYMENT_RETRY, EXPIRED));

    private final Set<BookingStatus> allowedTransitions;

    BookingStatus(Set<BookingStatus> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(BookingStatus newStatus) {
        return allowedTransitions.contains(newStatus);
    }
}
