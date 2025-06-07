package org.pdzsoftware.moviereservationsystem.repository;

import org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<User, Long> {
    @Query(value = """
                select new org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse(
                    m.id,
                    m.title,
                    m.description,
                    m.genre,
                    m.minutesDuration,
                    COUNT(s),
                    m.posterUrl,
                    m.releaseDate
                )
                FROM Movie m
                LEFT JOIN Session s 
                    ON s.movie = m 
                    AND s.startTime >= :threshold
                WHERE (:search IS NULL OR LOWER(m.title) LIKE LOWER(CAST(CONCAT('%', :search, '%') as text)))
                AND (:genres IS NULL OR m.genre IN :genres)
                GROUP BY m.id
                ORDER BY CASE WHEN COUNT(s) > 0 THEN 0 ELSE 1 END ASC, m.title ASC\s
            """,
            countQuery = """
                        SELECT COUNT(DISTINCT m)
                        FROM Movie m
                        WHERE (:search IS NULL OR LOWER(m.title) LIKE LOWER(CAST(CONCAT('%', :search, '%') as text)))
                        AND (:genres IS NULL OR m.genre IN :genres)
                    """
    )
    Page<MovieResponse> findResponsesByFilters(
            @Param("search") String search,
            @Param("genres") List<MovieGenre> genres,
            @Param("threshold") LocalDateTime threshold,
            Pageable pageable
    );

    @Query(value = """
                select new org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse(
                    m.id,
                    m.title,
                    m.description,
                    m.genre,
                    m.minutesDuration,
                    m.posterUrl,
                    m.releaseDate
                )
                FROM Movie m
                WHERE :id = m.id
            """
    )
    Optional<MovieResponse> findResponseById(@Param("id") Long id);

    @Query(value = """
                select new org.pdzsoftware.moviereservationsystem.dto.response.MovieResponse(
                    m.id,
                    m.title,
                    m.description,
                    m.genre,
                    m.minutesDuration,
                    m.posterUrl,
                    m.releaseDate
                )
                FROM Session s
                JOIN s.movie m
                WHERE :sessionId = s.id
            """
    )
    Optional<MovieResponse> findResponseBySessionId(@Param("sessionId") Long sessionId);
}
