package org.pdzsoftware.moviereservationsystem.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pdzsoftware.moviereservationsystem.dto.AuthTokensDto;
import org.pdzsoftware.moviereservationsystem.dto.request.LoginRequest;
import org.pdzsoftware.moviereservationsystem.dto.request.SignupRequest;
import org.pdzsoftware.moviereservationsystem.service.AuthService;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @GetMapping("/check")
    public ResponseEntity<Void> checkAuth(@CookieValue("refreshToken") String refreshToken) {
        if (!authService.isAuthenticated(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/ws")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> authForWebsocket(@CookieValue("accessToken") String accessToken,
                                                   @RequestParam String clientId) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.authForWebsocket(userId, clientId));
    }

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody @Valid SignupRequest request,
                                       HttpServletResponse response) {
        AuthTokensDto tokens = authService.signup(request);

        response.addCookie(getAccessTokenCookie(tokens));
        response.addCookie(getRefreshTokenCookie(tokens));

        return ResponseEntity.status(HttpStatus.CREATED).body(tokens.getUserId());
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request,
                                      HttpServletResponse response) {
        AuthTokensDto tokens = authService.login(request);

        response.addCookie(getAccessTokenCookie(tokens));
        response.addCookie(getRefreshTokenCookie(tokens));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refreshToken(@CookieValue("refreshToken") String refreshToken,
                                             HttpServletResponse response) {
        AuthTokensDto tokens = authService.refreshToken(refreshToken);

        response.addCookie(getAccessTokenCookie(tokens));
        response.addCookie(getRefreshTokenCookie(tokens));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken,
                                       HttpServletResponse response) {
        authService.revokeRefreshToken(refreshToken);

        response.addCookie(getInvalidatedAccessTokenCookie());
        response.addCookie(getInvalidatedRefreshTokenCookie());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private static Cookie getAccessTokenCookie(AuthTokensDto tokens) {
        Cookie accessTokenCookie = new Cookie("accessToken", tokens.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // Enable in production
        accessTokenCookie.setPath("/");
        accessTokenCookie.setAttribute("SameSite", "Strict");
        accessTokenCookie.setMaxAge(tokens.getAccessTokenExpirationMs());
        return accessTokenCookie;
    }

    private static Cookie getInvalidatedAccessTokenCookie() {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        return accessTokenCookie;
    }

    private static Cookie getRefreshTokenCookie(AuthTokensDto tokens) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // Enable in production
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        refreshTokenCookie.setMaxAge(tokens.getRefreshTokenExpirationMs());
        return refreshTokenCookie;
    }

    private static Cookie getInvalidatedRefreshTokenCookie() {
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        return refreshTokenCookie;
    }
}