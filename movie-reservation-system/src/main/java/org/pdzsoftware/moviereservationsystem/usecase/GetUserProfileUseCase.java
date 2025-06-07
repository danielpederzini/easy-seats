package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse;
import org.springframework.stereotype.Service;

@Service
public interface GetUserProfileUseCase {
    UserProfileResponse execute(Long userId);
}
