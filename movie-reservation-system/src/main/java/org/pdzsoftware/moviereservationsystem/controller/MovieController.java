package org.pdzsoftware.moviereservationsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.dto.response.SessionResponse;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.pdzsoftware.moviereservationsystem.usecase.GetMovieListUseCase;
import org.pdzsoftware.moviereservationsystem.usecase.GetMovieSessionsUseCase;
import org.pdzsoftware.moviereservationsystem.usecase.GetMovieUseCase;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private final GetMovieListUseCase getMovieList;
    private final GetMovieUseCase getMovie;
    private final GetMovieSessionsUseCase getMovieSessions;

    @GetMapping
    public ResponseEntity<Page<MovieResponse>> getMovieList(@RequestParam(required = false) String search,
                                                            @RequestParam(required = false) List<MovieGenre> genres,
                                                            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.status(HttpStatus.OK).body(getMovieList.execute(search, genres, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovie(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(getMovie.execute(id));
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<Page<SessionResponse>> getMovieSessions(@PathVariable Long id,
                                                                  @RequestParam(required = false)
                                                                  @DateTimeFormat(pattern = "yyyy/MM/dd")
                                                                  LocalDate sessionDate,
                                                                  @RequestParam(required = false) Long theaterId,
                                                                  @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.status(HttpStatus.OK).body(getMovieSessions.execute(id, sessionDate, theaterId, page));
    }
}
