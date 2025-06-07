package org.pdzsoftware.moviereservationsystem.repository;

import org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
                SELECT u
                FROM User u
                WHERE u.refreshToken IS NOT NULL
                AND u.refreshToken = :refreshToken
            """)
    Optional<User> findByRefreshToken(String refreshToken);


    @Query("""
                SELECT u.id
                FROM Booking b
                JOIN b.user u
                WHERE b.id = :bookingId
            """)
    Optional<Long> findIdByBookingId(@Param("bookingId") Long bookingId);

    @Query("""
                select new org.pdzsoftware.moviereservationsystem.dto.response.UserProfileResponse(
                    u.id,
                    u.userName,
                    u.email,
                    u.userRole
                )
                FROM User u
                WHERE u.id = :id
            """)
    Optional<UserProfileResponse> findProfileById(@Param("id") Long id);
}
