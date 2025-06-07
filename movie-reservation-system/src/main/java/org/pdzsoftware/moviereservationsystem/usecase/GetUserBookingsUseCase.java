package org.pdzsoftware.moviereservationsystem.usecase;

import org.pdzsoftware.moviereservationsystem.dto.response.BookingDetailedResponse;
import org.pdzsoftware.moviereservationsystem.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GetUserBookingsUseCase {
    Page<BookingDetailedResponse> execute(Long userId, List<BookingStatus> statuses, int page);
}
