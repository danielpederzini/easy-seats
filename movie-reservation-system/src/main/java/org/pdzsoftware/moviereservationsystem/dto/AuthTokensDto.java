package org.pdzsoftware.moviereservationsystem.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokensDto {
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private int accessTokenExpirationMs;
    private int refreshTokenExpirationMs;
}
