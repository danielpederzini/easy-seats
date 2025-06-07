package org.pdzsoftware.moviereservationsystem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    @NotNull(message = "SessionID must not be null")
    private Long sessionId;

    @NotEmpty(message = "Booking request must include at least one seat")
    private Set<Long> seatIds;

    @URL(message = "Success URL must be valid")
    private String successUrl;

    @URL(message = "Cancel URL must be valid")
    private String cancelUrl;
}
