package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatUpdateResponse {
    private Long id;
    private String originId;
    private boolean taken;
}
