package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GetMovieListUseCase {
    Page<MovieResponse> execute(String search, List<MovieGenre> genres, int page);
}
