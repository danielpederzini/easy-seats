package org.pdzsoftware.moviereservationsystem.service;

import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface MovieService {
    Optional<MovieResponse> findResponseById(Long movieId);
    Optional<MovieResponse> findResponseBySessionId(Long sessionId);
    Page<MovieResponse> findResponsesByFilters(String search, List<MovieGenre> genres, int page);
}
