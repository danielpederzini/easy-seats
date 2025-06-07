package org.pdzsoftware.moviereservationsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutInfoRequest {
    @NotBlank(message = "Checkout ID must not be blank")
    private String checkoutId;
}
