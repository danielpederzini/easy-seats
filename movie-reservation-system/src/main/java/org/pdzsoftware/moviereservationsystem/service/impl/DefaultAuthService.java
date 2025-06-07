package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.AuthTokensDto;
import org.pdzsoftware.moviereservationsystem.dto.request.LoginRequest;
import org.pdzsoftware.moviereservationsystem.dto.request.SignupRequest;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.UnauthorizedException;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.repository.UserRepository;
import org.pdzsoftware.moviereservationsystem.service.AuthService;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public boolean isAuthenticated(String refreshToken) {
        if (!jwtUtils.isJwtValid(refreshToken)) {
            return false;
        }

        return userRepository.findByRefreshToken(refreshToken).isPresent();
    }

    @Override
    public String authForWebsocket(Long userId, String clientId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UnauthorizedException("Websocket auth failed, user not found"));

        return jwtUtils.generateWebsocketToken(user, clientId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthTokensDto signup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use");
        }

        String passwordHash = hashPassword(signupRequest.getPassword());
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .userName(signupRequest.getName())
                .email(email)
                .passwordHash(passwordHash)
                .userRole(UserRole.CUSTOMER)
                .createdAt(now)
                .build();

        User savedUser = userRepository.save(user);

        log.info("[DefaultAuthService] User with ID: {} created", savedUser.getId());
        return generateAuthInformation(savedUser);
    }

    @Override
    public AuthTokensDto login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().toLowerCase();
        String password = loginRequest.getPassword();

        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UnauthorizedException("User not found for provided credentials"));

        return generateAuthInformation(user);
    }

    @Override
    public AuthTokensDto refreshToken(String refreshToken) {
        if (!jwtUtils.isJwtValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = userRepository.findByRefreshToken(refreshToken).orElseThrow(() ->
                new UnauthorizedException("User not found for provided refresh token"));

        return generateAuthInformation(user);
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);
        if (user == null) return;

        user.setRefreshToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private AuthTokensDto generateAuthInformation(User user) {
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);

        user.setRefreshToken(refreshToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return AuthTokensDto.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpirationMs(jwtUtils.getAccessTokenExpirationMs())
                .refreshTokenExpirationMs(jwtUtils.getRefreshTokenExpirationMs())
                .build();
    }
}
