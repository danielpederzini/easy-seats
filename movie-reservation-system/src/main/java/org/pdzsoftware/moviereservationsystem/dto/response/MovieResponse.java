package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.pdzsoftware.moviereservationsystem.enums.MovieGenre;

import java.time.LocalDate;

@Getter
@Setter
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private MovieGenre genre;
    private String formattedDuration;
    private boolean hasSessions;
    private String posterUrl;
    private LocalDate releaseDate;

    public MovieResponse(Long id,
                         String title,
                         String description,
                         MovieGenre genre,
                         int minutesDuration,
                         long sessionCount,
                         String posterUrl,
                         LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.formattedDuration = String.format("%dh%02dm", minutesDuration / 60, minutesDuration % 60);
        this.hasSessions = sessionCount > 0;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
    }

    public MovieResponse(Long id,
                         String title,
                         String description,
                         MovieGenre genre,
                         int minutesDuration,
                         String posterUrl,
                         LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.formattedDuration = String.format("%dh%02dm", minutesDuration / 60, minutesDuration % 60);
        this.hasSessions = true;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
    }
}
