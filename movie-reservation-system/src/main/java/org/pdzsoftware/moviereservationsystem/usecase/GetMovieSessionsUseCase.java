package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface GetMovieSessionsUseCase {
    Page<SessionResponse> execute(Long movieId, LocalDate sessionDate, Long theaterId, int page);
}
