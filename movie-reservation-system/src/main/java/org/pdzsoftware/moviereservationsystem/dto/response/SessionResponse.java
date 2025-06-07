package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.pdzsoftware.moviereservationsystem.enums.Language;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SessionResponse {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Language audioLanguage;
    private boolean hasSubtitles;
    private boolean isThreeD;
    private BigDecimal standardSeatPrice;
    private BigDecimal vipSeatPrice;
    private BigDecimal pwdSeatPrice;
    private Long theaterId;
    private String theaterName;
    private String theaterLogoUrl;
    private String theaterAddress;
    private String screenName;
    private boolean hasFreeSeats;

    public SessionResponse(Long id,
                           LocalDateTime startTime,
                           LocalDateTime endTime,
                           Language audioLanguage,
                           boolean hasSubtitles,
                           boolean isThreeD,
                           BigDecimal standardSeatPrice,
                           BigDecimal vipSeatPrice,
                           BigDecimal pwdSeatPrice,
                           Long theaterId,
                           String theaterName,
                           String theaterLogoUrl,
                           String theaterAddress,
                           String screenName) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.audioLanguage = audioLanguage;
        this.hasSubtitles = hasSubtitles;
        this.isThreeD = isThreeD;
        this.standardSeatPrice = standardSeatPrice;
        this.vipSeatPrice = vipSeatPrice;
        this.pwdSeatPrice = pwdSeatPrice;
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.theaterLogoUrl = theaterLogoUrl;
        this.theaterAddress = theaterAddress;
        this.screenName = screenName;
    }
}
