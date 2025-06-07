package org.pdzsoftware.moviereservationsystem.dto.event;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CacheSeatStatusUpdateEvent {
    private Long id;
    private Long sessionId;
    private String originId;
    private boolean taken;
}
