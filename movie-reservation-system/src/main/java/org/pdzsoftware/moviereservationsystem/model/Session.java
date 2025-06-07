package org.pdzsoftware.moviereservationsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.pdzsoftware.moviereservationsystem.enums.Language;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Session")
@Table(name = "tb_session")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    private Language audioLanguage;
    private boolean hasSubtitles;
    private boolean isThreeD;
    private BigDecimal standardSeatPrice;
    private BigDecimal vipSeatPrice;
    private BigDecimal pwdSeatPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Many sessions can belong to one movie
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_fk")
    private Movie movie;

    // Many sessions can belong to one screen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_fk")
    private Screen screen;

    // One session can have many bookings
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
}
