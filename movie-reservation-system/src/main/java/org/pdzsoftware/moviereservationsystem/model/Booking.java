package org.pdzsoftware.moviereservationsystem.model;

import jakarta.persistence.*;
import lombok.*;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Booking")
@Table(name = "tb_booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
    private BigDecimal totalPrice;
    private String paymentIntentId;
    private String refundId;
    private String checkoutId;
    @Column(length = 1024)
    private String checkoutUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private boolean qrCodeValidated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_fk")
    private Session session;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookedSeat> bookedSeats = new ArrayList<>();
}
