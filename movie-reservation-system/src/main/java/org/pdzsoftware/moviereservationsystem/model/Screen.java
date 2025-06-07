package org.pdzsoftware.moviereservationsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Screen")
@Table(name = "tb_screen")
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String screenName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Many screens can belong to one theater
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_fk")
    private Theater theater;

    // One screen can have many seats
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    // One screen can have many sessions
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Session> sessions = new ArrayList<>();
}
