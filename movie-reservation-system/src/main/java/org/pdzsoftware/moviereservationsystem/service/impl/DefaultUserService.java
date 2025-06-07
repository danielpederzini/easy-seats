package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.pdzsoftware.moviereservationsystem.dto.CustomUserDetailsDto;
import org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.pdzsoftware.moviereservationsystem.repository.UserRepository;
import org.pdzsoftware.moviereservationsystem.service.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserDetailsService, UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<Long> findIdByBookingId(Long bookingId) {
        return userRepository.findIdByBookingId(bookingId);
    }

    @Override
    public Optional<UserProfileResponse> getProfileFromId(Long userId) {
        return userRepository.findProfileById(userId);
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public CustomUserDetailsDto loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetailsDto(user);
    }
}
