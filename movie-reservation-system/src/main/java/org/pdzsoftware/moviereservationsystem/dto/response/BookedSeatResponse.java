package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.pdzsoftware.moviereservationsystem.enums.SeatType;

import java.math.BigDecimal;

@Getter
@Setter
public class BookedSeatResponse {
    private Long id;
    private String seatRow;
    private int seatNumber;
    private SeatType seatType;
    private BigDecimal seatPrice;

    public BookedSeatResponse(Long id,
                              String seatRow,
                              int seatNumber,
                              SeatType seatType,
                              BigDecimal seatPrice) {
        this.id = id;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.seatPrice = seatPrice;
    }
}
