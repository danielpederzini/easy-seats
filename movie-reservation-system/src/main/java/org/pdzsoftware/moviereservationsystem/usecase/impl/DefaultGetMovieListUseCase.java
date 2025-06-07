package org.pdzsoftware.moviereservationsystem.usecase.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.pdzsoftware.moviereservationsystem.service.MovieService;
import org.pdzsoftware.moviereservationsystem.usecase.GetMovieListUseCase;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultGetMovieListUseCase implements GetMovieListUseCase {
    private final MovieService movieService;

    @Override
    public Page<MovieResponse> execute(String search, List<MovieGenre> genres, int page) {
        return movieService.findResponsesByFilters(search, genres, page);
    }
}
