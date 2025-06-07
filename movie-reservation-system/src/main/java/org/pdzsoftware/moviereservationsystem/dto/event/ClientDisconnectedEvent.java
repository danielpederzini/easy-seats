package org.pdzsoftware.moviereservationsystem.dto.event;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientDisconnectedEvent {
    private Long userId;
    private Long sessionId;
    private String originId;
}
