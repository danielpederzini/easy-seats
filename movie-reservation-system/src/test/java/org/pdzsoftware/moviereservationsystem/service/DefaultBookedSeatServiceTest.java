package org.pdzsoftware.moviereservationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pdzsoftware.moviereservationsystem.repository.BookedSeatRepository;
import org.pdzsoftware.moviereservationsystem.service.impl.DefaultBookedSeatService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultBookedSeatServiceTest {
    @Mock
    private BookedSeatRepository bookedSeatRepository;
    @InjectMocks
    private DefaultBookedSeatService bookedSeatService;

    @Test
    void findResponsesByBookingId_always_callsRepositoryWithoutAlteringArguments() {
        // Arrange
        Long bookingId = 1L;

        // Act
        bookedSeatService.findResponsesByBookingId(bookingId);

        // Assert
        verify(bookedSeatRepository).findResponseByBookingId(eq(bookingId));
    }
}
