package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.pdzsoftware.moviereservationsystem.enums.SeatType;

@Getter
@Setter
public class SeatResponse {
    private Long id;
    private String seatRow;
    private int seatNumber;
    private SeatType seatType;
    private boolean isTaken;

    public SeatResponse(Long id, String seatRow, int seatNumber, SeatType seatType) {
        this.id = id;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }
}
