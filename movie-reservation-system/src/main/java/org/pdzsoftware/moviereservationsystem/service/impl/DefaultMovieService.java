package org.pdzsoftware.moviereservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.pdzsoftware.moviereservationsystem.repository.MovieRepository;
import org.pdzsoftware.moviereservationsystem.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultMovieService implements MovieService {
    private static final Duration TOLERATED_PERIOD = Duration.ofMinutes(20L);

    private final MovieRepository movieRepository;

    @Override
    public Optional<MovieResponse> findResponseById(Long movieId) {
        return movieRepository.findResponseById(movieId);
    }

    @Override
    public Optional<MovieResponse> findResponseBySessionId(Long sessionId) {
        return movieRepository.findResponseBySessionId(sessionId);
    }

    @Override
    public Page<MovieResponse> findResponsesByFilters(String search,
                                                      List<MovieGenre> genres,
                                                      int page) {
        if (genres != null && genres.isEmpty()) genres = null;

        Pageable pageable = PageRequest.of(page, 15);
        LocalDateTime threshold = LocalDateTime.now().minus(TOLERATED_PERIOD);

        return movieRepository.findResponsesByFilters(search, genres, threshold, pageable);
    }
}
