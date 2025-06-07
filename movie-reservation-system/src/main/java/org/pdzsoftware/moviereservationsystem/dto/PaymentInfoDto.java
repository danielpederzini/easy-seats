package org.pdzsoftware.moviereservationsystem.dto;

import lombok.*;
import org.pdzsoftware.moviereservationsystem.enums.CheckoutStatus;
import org.pdzsoftware.moviereservationsystem.enums.PaymentStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfoDto {
    private String checkoutId;
    private String paymentIntentId;
    private CheckoutStatus checkoutStatus;
    private PaymentStatus paymentStatus;
}
