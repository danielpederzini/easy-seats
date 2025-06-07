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
@Entity(name = "Theater")
@Table(name = "tb_theater")
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String theaterName;
    private String fullAddressLine;
    @Column(length = 2048)
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // One theater can have many screens
    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Screen> screens = new ArrayList<>();
}
