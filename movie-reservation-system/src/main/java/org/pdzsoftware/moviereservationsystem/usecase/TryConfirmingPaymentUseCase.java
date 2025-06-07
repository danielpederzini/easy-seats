package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.request.CheckoutInfoRequest;
import org.springframework.stereotype.Service;

@Service
public interface TryConfirmingPaymentUseCase {
    boolean execute(Long userId, Long bookingId, CheckoutInfoRequest checkoutInfoRequest);
}
