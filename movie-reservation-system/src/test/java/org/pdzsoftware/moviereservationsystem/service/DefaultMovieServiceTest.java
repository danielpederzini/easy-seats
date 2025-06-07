package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.pdzsoftware.moviereservationsystem.repository.MovieRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultMovieService;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.pdzsoftware.moviereservationsystem.enums.MovieGenre.ACTION;

@ExtendWith(MockitoExtension.class)
class DefaultMovieServiceTest {
    @Mock
    private MovieRepository movieRepository;
    @InjectMocks
    private DefaultMovieService movieService;

    @Test
    void findResponseById_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long movieId = 1L;

        // Act
        movieService.findResponseById(movieId);

        // Assert
        verify(movieRepository).findResponseById(eq(movieId));
    }

    @Test
    void findResponseBySessionId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long sessionId = 1L;

        // Act
        movieService.findResponseBySessionId(sessionId);

        // Assert
        verify(movieRepository).findResponseBySessionId(eq(sessionId));
    }

    @Test
    void findResponsesByFilters_withEmptyGenresList_callsRepositoryAlteringOnlyGenres() {
        // Arrange
        String search = "search";
        List<MovieGenre> genres = List.of();
        int page = 0;

        // Act
        movieService.findResponsesByFilters(search, genres, page);

        // Assert
        verify(movieRepository).findResponsesByFilters(
                eq(search), eq(null), any(LocalDateTime.class), any(Pageable.class)
        );
    }

    @Test
    void findResponsesByFilters_withGenreList_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        String search = "search";
        List<MovieGenre> genres = List.of(ACTION);
        int page = 0;

        // Act
        movieService.findResponsesByFilters(search, genres, page);

        // Assert
        verify(movieRepository).findResponsesByFilters(
                eq(search), eq(genres), any(LocalDateTime.class), any(Pageable.class)
        );
    }
}
