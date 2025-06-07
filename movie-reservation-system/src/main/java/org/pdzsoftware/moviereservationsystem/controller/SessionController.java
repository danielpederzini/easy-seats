package org.pdzsoftware.moviereservationsystem.controller;

import lombok.RequiredArgsConstructor;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse;
import org.pdzsoftware.moviereservationsystem.usecase.GetSessionDetailedUseCase;
import org.pdzsoftware.moviereservationsystem.usecase.ReleaseSeatFromCacheUseCase;
import org.pdzsoftware.moviereservationsystem.usecase.ReserveSeatInCacheUseCase;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final GetSessionDetailedUseCase getSessionDetailed;
    private final ReserveSeatInCacheUseCase reserveSeatInCache;
    private final ReleaseSeatFromCacheUseCase releaseSeatFromCache;

    private final JwtUtils jwtUtils;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SessionDetailedResponse> getSessionDetailed(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(getSessionDetailed.execute(id));
    }

    @PostMapping("/{id}/seats/{seatId}/cache")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> reserveSeatInCache(@CookieValue("accessToken") String accessToken,
                                                   @PathVariable Long id,
                                                   @PathVariable Long seatId,
                                                   @RequestParam String clientId) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        reserveSeatInCache.execute(userId, id, seatId, clientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}/seats/{seatId}/cache")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> releaseSeatFromCache(@CookieValue("accessToken") String accessToken,
                                                     @PathVariable Long id,
                                                     @PathVariable Long seatId,
                                                     @RequestParam String clientId) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        releaseSeatFromCache.execute(userId, id, seatId, clientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
