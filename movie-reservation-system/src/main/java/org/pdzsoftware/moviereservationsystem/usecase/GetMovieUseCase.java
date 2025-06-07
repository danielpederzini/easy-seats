package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.springframework.stereotype.Service;

@Service
public interface GetMovieUseCase {
    MovieResponse execute(Long movieId);
}
