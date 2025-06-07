package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.AuthTokensDto;
import org.pdzsoftware.moviereservationsystem.dto.request.LoginRequest;
import org.pdzsoftware.moviereservationsystem.dto.request.SignupRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    boolean isAuthenticated(String refreshToken);
    String authForWebsocket(Long userId, String clientId);
    AuthTokensDto signup(SignupRequest signupRequest);
    AuthTokensDto login(LoginRequest loginRequest);
    AuthTokensDto refreshToken(String refreshToken);
    void revokeRefreshToken(String refreshToken);
}
