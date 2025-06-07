package org.pdzsoftware.moviereservationsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.pdzsoftware.moviereservationsystem.enums.SeatType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Seat")
@Table(name = "tb_seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatRow;
    private int seatNumber;
    @Enumerated(EnumType.STRING)
    private SeatType seatType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Many seats can belong to one screen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_fk")
    private Screen screen;

    // One seat can have many bookings
    @OneToMany(mappedBy = "seat")
    private List<BookedSeat> bookings = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("{ ID: %s, Seat Row: %s, Seat Number: %s }", id, seatRow, seatNumber);
    }
}
