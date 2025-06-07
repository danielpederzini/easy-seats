package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {
    Optional<User> findById(Long userId);
    Optional<Long> findIdByBookingId(Long bookingId);
    Optional<UserProfileResponse> getProfileFromId(Long userId);
    boolean existsById(Long userId);
}
