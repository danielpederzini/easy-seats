package org.pdzsoftware.moviereservationsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "User")
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private String email;
    private String passwordHash;
    @Column(length = 1024)
    private String refreshToken;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // One user can have many bookings
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
}
