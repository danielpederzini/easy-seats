package org.pdzsoftware.moviereservationsystem.controller;

import lombok.RequiredArgsConstructor;
import org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse;
import org.pdzsoftware.moviereservationsystem.usecase.GetUserProfileUseCase;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetUserProfileUseCase getUserProfile;

    private final JwtUtils jwtUtils;

    @GetMapping("/fromToken")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserProfileResponse> getUserProfileFromToken(@CookieValue("accessToken") String accessToken) {
        Long userId = jwtUtils.getUserIdFromToken(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(getUserProfile.execute(userId));
    }
}