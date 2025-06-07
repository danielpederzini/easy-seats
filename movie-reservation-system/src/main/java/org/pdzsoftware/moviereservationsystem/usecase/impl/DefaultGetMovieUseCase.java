package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.NotFoundException;
import org.pdzsoftware.moviereservationsystem.service.MovieService;
import org.pdzsoftware.moviereservationsystem.usecase.GetMovieUseCase;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGetMovieUseCase implements GetMovieUseCase {
    private final MovieService movieService;

    @Override
    public MovieResponse execute(Long movieId) {
        return movieService.findResponseById(movieId).orElseThrow(() ->
                new NotFoundException("Movie not found for given ID")
        );
    }
}
