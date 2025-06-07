package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.response.SessionDetailedResponse;
import org.springframework.stereotype.Service;

@Service
public interface GetSessionDetailedUseCase {
    SessionDetailedResponse execute(Long sessionId);
}
