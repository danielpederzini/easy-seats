package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.dto.CustomUserDetailsDto;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;
import org.pdzsoftware.moviereservationsystem.exception.custom.ConflictException;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.repository.MovieRepository;
import org.pdzsoftware.moviereservationsystem.repository.UserRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pdzsoftware.moviereservationsystem.enums.MovieGenre.ACTION;

@ExtendWith(MockitoExtension.class)
class DefaultUserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private DefaultUserService userService;

    @Test
    void findById_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long userId = 1L;

        // Act
        userService.findById(userId);

        // Assert
        verify(userRepository).findById(eq(userId));
    }

    @Test
    void findIdByBookingId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long bookingId = 1L;

        // Act
        userService.findIdByBookingId(bookingId);

        // Assert
        verify(userRepository).findIdByBookingId(eq(bookingId));
    }

    @Test
    void getProfileFromId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long userId = 1L;

        // Act
        userService.getProfileFromId(userId);

        // Assert
        verify(userRepository).findProfileById(eq(userId));
    }

    @Test
    void existsById_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long userId = 1L;

        // Act
        userService.existsById(userId);

        // Assert
        verify(userRepository).existsById(eq(userId));
    }

    @Test
    void loadUserByUsername_withExistingUser_returnsCustomUserDetailsDto() {
        // Arrange
        String email = "test@example.com";
        User user = getMockUser();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        // Act
        CustomUserDetailsDto result = userService.loadUserByUsername(email);

        // Assert
        assertEquals(result.getUserId(), user.getId());
        assertEquals(result.getUsername(), user.getEmail());
        assertEquals(result.getPassword(), user.getPasswordHash());
        assertEquals(result.getAuthorities(), List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())));

        verify(userRepository).findByEmail(eq(email));
    }

    @Test
    void loadUserByUsername_withNonExistingUser_throwsUsernameNotFoundException() {
        // Arrange
        String email = "test@example.com";
        User user = getMockUser();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(userRepository).findByEmail(eq(email));
    }

    private static User getMockUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("oqwdk012d1$$@!ds")
                .userRole(UserRole.CUSTOMER)
                .build();
    }
}
