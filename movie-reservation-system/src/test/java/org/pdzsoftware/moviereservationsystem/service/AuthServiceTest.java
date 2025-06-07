package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.dto.AuthTokensDto;
import org.pdzsoftware.moviereservationsystem.dto.request.LoginRequest;
import org.pdzsoftware.moviereservationsystem.dto.request.SignupRequest;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.exception.custom.UnauthorizedException;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.repository.UserRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultAuthService;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;
    @InjectMocks
    private DefaultAuthService authService;

    @Test
    void isAuthenticated_withActiveToken_returnsTrue() {
        // Arrange
        String refreshToken = "refresh-token";
        when(jwtUtils.isJwtValid(refreshToken)).thenReturn(true);
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(getMockUser()));

        // Act
        boolean isAuthenticated = authService.isAuthenticated(refreshToken);

        // Assert
        assertTrue(isAuthenticated);
        verify(jwtUtils).isJwtValid(refreshToken);
        verify(userRepository).findByRefreshToken(refreshToken);
    }

    @Test
    void isAuthenticated_withInvalidToken_returnsFalse() {
        // Arrange
        String refreshToken = "refresh-token";
        when(jwtUtils.isJwtValid(refreshToken)).thenReturn(false);

        // Act
        boolean isAuthenticated = authService.isAuthenticated(refreshToken);

        // Assert
        assertFalse(isAuthenticated);
        verify(jwtUtils).isJwtValid(refreshToken);
    }

    @Test
    void isAuthenticated_withRevokedToken_returnsFalse() {
        // Arrange
        String refreshToken = "refresh-token";
        when(jwtUtils.isJwtValid(refreshToken)).thenReturn(true);
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        // Act
        boolean isAuthenticated = authService.isAuthenticated(refreshToken);

        // Assert
        assertFalse(isAuthenticated);
        verify(jwtUtils).isJwtValid(refreshToken);
        verify(userRepository).findByRefreshToken(refreshToken);
    }

    @Test
    void authForWebsocket_withValidUserId_returnsWebsocketToken() {
        // Arrange
        Long userId = 1L;
        String clientId = "client-id";
        String expected = "websocket-token";

        when(userRepository.findById(userId)).thenReturn(Optional.of(getMockUser()));
        when(jwtUtils.generateWebsocketToken(any(User.class), eq(clientId))).thenReturn(expected);

        // Act
        String actual = authService.authForWebsocket(userId, clientId);

        // Assert
        assertEquals(expected, actual);
        verify(userRepository).findById(userId);
        verify(jwtUtils).generateWebsocketToken(any(User.class), eq(clientId));
    }

    @Test
    void authForWebsocket_withInvalidUserId_throwsUnauthorizedException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        assertThatThrownBy(() -> authService.authForWebsocket(userId, "client-id"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Websocket auth failed, user not found");

        // Assert
        verify(userRepository).findById(userId);
        verify(jwtUtils, never()).generateWebsocketToken(any(User.class), any(String.class));
    }

    @Test
    void createUser_withValidInput_returnsAuthTokens() {
        // Arrange
        SignupRequest request = getMockSignupRequest();
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("091i0dqww$@!#");
        when(userRepository.save(any(User.class))).thenReturn(getMockUser());

        when(jwtUtils.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(300_000);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(86_400_000);

        // Act
        AuthTokensDto authTokens = authService.signup(request);

        // Assert
        assertNotNull(authTokens);
        assertEquals(1L, authTokens.getUserId());
        assertEquals("access-token", authTokens.getAccessToken());
        assertEquals("refresh-token", authTokens.getRefreshToken());
        assertEquals(300_000, authTokens.getAccessTokenExpirationMs());
        assertEquals(86_400_000, authTokens.getRefreshTokenExpirationMs());

        verify(jwtUtils).generateAccessToken(any(User.class));
        verify(jwtUtils).generateRefreshToken(any(User.class));
        verify(userRepository).existsByEmail(request.getEmail().toLowerCase());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void createUser_withDuplicateEmail_throwsConflictException() {
        // Arrange
        SignupRequest request = getMockSignupRequest();
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository).existsByEmail(request.getEmail().toLowerCase());
    }

    @Test
    void login_withValidCredentials_returnsAuthTokens() {
        // Arrange
        LoginRequest request = getMockLoginRequest();
        when(authManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(getMockUser()));

        when(jwtUtils.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(300_000);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(86_400_000);

        when(userRepository.save(any(User.class))).thenReturn(getMockUser());

        // Act
        AuthTokensDto authTokens = authService.login(request);

        // Assert
        assertNotNull(authTokens);
        assertEquals(1L, authTokens.getUserId());
        assertEquals("access-token", authTokens.getAccessToken());
        assertEquals("refresh-token", authTokens.getRefreshToken());
        assertEquals(300_000, authTokens.getAccessTokenExpirationMs());
        assertEquals(86_400_000, authTokens.getRefreshTokenExpirationMs());

        verify(authManager).authenticate(any(Authentication.class));
        verify(userRepository).findByEmail(request.getEmail().toLowerCase());
        verify(jwtUtils).generateAccessToken(any(User.class));
        verify(jwtUtils).generateRefreshToken(any(User.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_withInvalidCredentials_throwsAuthenticationException() {
        // Arrange
        LoginRequest request = getMockLoginRequest();
        when(authManager.authenticate(any(Authentication.class))).thenThrow(BadCredentialsException.class);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthenticationException.class);

        // Assert
        verify(authManager).authenticate(any(Authentication.class));
    }

    @Test
    void refreshToken_withValidToken_returnsAuthTokens() {
        // Arrange
        String refreshToken = "refresh-token";
        when(jwtUtils.isJwtValid(refreshToken)).thenReturn(true);
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(getMockUser()));

        when(jwtUtils.generateAccessToken(any(User.class))).thenReturn("access-token2");
        when(jwtUtils.generateRefreshToken(any(User.class))).thenReturn("refresh-token2");
        when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(300_000);
        when(jwtUtils.getRefreshTokenExpirationMs()).thenReturn(86_400_000);

        when(userRepository.save(any(User.class))).thenReturn(getMockUser());

        // Act
        AuthTokensDto authTokens = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(authTokens);
        assertEquals(1L, authTokens.getUserId());
        assertEquals("access-token2", authTokens.getAccessToken());
        assertEquals("refresh-token2", authTokens.getRefreshToken());
        assertEquals(300_000, authTokens.getAccessTokenExpirationMs());
        assertEquals(86_400_000, authTokens.getRefreshTokenExpirationMs());

        verify(jwtUtils).isJwtValid(refreshToken);
        verify(userRepository).findByRefreshToken(refreshToken);
        verify(jwtUtils).generateAccessToken(any(User.class));
        verify(jwtUtils).generateRefreshToken(any(User.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void refreshToken_withInvalidToken_throwsUnauthorizedException() {
        // Arrange
        String refreshToken = "refresh-token";
        when(jwtUtils.isJwtValid(refreshToken)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(jwtUtils).isJwtValid(refreshToken);
    }

    @Test
    void refreshToken_withRevokedToken_throwsUnauthorizedException() {
        // Arrange
        String refreshToken = "refresh-token";
        when(jwtUtils.isJwtValid(refreshToken)).thenReturn(true);
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User not found for provided refresh token");

        verify(jwtUtils).isJwtValid(refreshToken);
        verify(userRepository).findByRefreshToken(refreshToken);
    }

    @Test
    void revokeRefreshToken_withActiveToken_revokesToken() {
        // Arrange
        User user = getMockUserWithRefreshToken();
        String refreshToken = "refresh-token";
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(user));

        // Act
        authService.revokeRefreshToken(refreshToken);

        // Assert
        verify(userRepository).findByRefreshToken(refreshToken);
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRefreshToken() == null));
    }

    @Test
    void revokeRefreshToken_withRevokedToken_doesNothing() {
        // Arrange
        String refreshToken = "refresh-token";
        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        // Act
        authService.revokeRefreshToken(refreshToken);

        // Assert
        verify(userRepository).findByRefreshToken(refreshToken);
        verify(userRepository, never()).save(any(User.class));
    }

    private static LoginRequest getMockLoginRequest() {
        return new LoginRequest("test@example.com", "Test1234");
    }

    private static SignupRequest getMockSignupRequest() {
        return new SignupRequest("test", "test@example.com", "Test1234");
    }

    private static User getMockUser() {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .id(1L)
                .userName("test")
                .email("TeSt@eXaMpLe.com")
                .passwordHash("091i0dqww$@!#")
                .refreshToken(null)
                .userRole(UserRole.CUSTOMER)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static User getMockUserWithRefreshToken() {
        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .id(1L)
                .userName("test")
                .email("test@example.com")
                .passwordHash("091i0dqww$@!#")
                .refreshToken("refresh-token")
                .userRole(UserRole.CUSTOMER)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
