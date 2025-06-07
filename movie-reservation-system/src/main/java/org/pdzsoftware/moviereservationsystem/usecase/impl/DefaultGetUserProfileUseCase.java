package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.service.UserService;
import org.pdzsoftware.moviereservationsystem.usecase.GetUserProfileUseCase;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGetUserProfileUseCase implements GetUserProfileUseCase {
    private final UserService userService;

    @Override
    public UserProfileResponse execute(Long userId) {
        return userService.getProfileFromId(userId).orElseThrow(() ->
                new NotFoundException("Profile not found for given user ID")
        );
    }
}
