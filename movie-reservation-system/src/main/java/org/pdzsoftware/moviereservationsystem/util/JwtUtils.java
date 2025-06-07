package org.pdzsoftware.moviereservationsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.custom.InternalErrorException;
import org.pdzsoftware.moviereservationsystem.exception.custom.UnauthorizedException;
import org.pdzsoftware.moviereservationsystem.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtils {
    public static final String USER_ID = "userId";
    public static final String BOOKING_ID = "bookingId";
    public static final String CLIENT_ID = "clientId";
    public static final String ROLE = "role";
    public static final String ISS = "iss";
    public static final String AUD = "aud";
    public static final String JTI = "jti";

    public static final String ALGORITHM = "RSA";

    public static final int QR_CODE_EXPIRATION_MS = 10 * 60 * 1000;

    @Value("${app.jwt.keys.public}")
    private String publicKey;
    @Value("${app.jwt.keys.private}")
    private String privateKey;

    @Value("${app.jwt.issuer}")
    private String issuer;
    @Value("${app.jwt.audience}")
    private String audience;

    @Getter
    @Value("${app.jwt.access-token.expiration-ms}")
    private int accessTokenExpirationMs;
    @Getter
    @Value("${app.jwt.refresh-token.expiration-ms}")
    private int refreshTokenExpirationMs;
    @Value("${app.jwt.websocket-token.expiration-ms}")
    private int websocketTokenExpirationMs;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(USER_ID, user.getId())
                .claim(ROLE, "ROLE_" + user.getUserRole().name())
                .claim(ISS, issuer)
                .claim(AUD, audience)
                .claim(JTI, UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(getPrivateKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(ISS, issuer)
                .claim(AUD, audience)
                .claim(JTI, UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
                .signWith(getPrivateKey())
                .compact();
    }

    public String generateWebsocketToken(User user, String clientId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim(USER_ID, user.getId())
                .claim(CLIENT_ID, clientId)
                .claim(ISS, issuer)
                .claim(AUD, audience)
                .claim(JTI, UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(websocketTokenExpirationMs)))
                .signWith(getPrivateKey())
                .compact();
    }

    public String generateQrCode(Long bookingId, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim(USER_ID, userId)
                .claim(BOOKING_ID, bookingId)
                .claim(JTI, UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(QR_CODE_EXPIRATION_MS)))
                .signWith(getPrivateKey())
                .compact();
    }

    public String parseJwt(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public String parseJwt(String requestHeader) {
        if (StringUtils.hasText(requestHeader) && requestHeader.startsWith("Bearer ")) {
            return requestHeader.substring(7);
        }
        return null;
    }

    public boolean isJwtValid(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return isIssuerValid(claims) && isAudienceValid(claims) && isIdPresent(claims) && !isTokenExpired(claims);
    }

    public boolean isQrCodeValid(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return isIdPresent(claims) && !isTokenExpired(claims);
    }

    private boolean isIssuerValid(Claims claims) {
        return issuer.equals(claims.getIssuer());
    }

    private boolean isAudienceValid(Claims claims) {
        return claims.getAudience().contains(audience);
    }

    private boolean isIdPresent(Claims claims) {
        return StringUtils.hasText(claims.getId());
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(Date.from(Instant.now()));
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid JWT token");
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get(USER_ID, Long.class);
    }

    private PublicKey getPublicKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(publicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(ALGORITHM).generatePublic(spec);
        } catch (Exception ex) {
            log.error("[JwtUtils] Error while generating public key", ex);
            throw new InternalErrorException("Error generating key, contact administrator");
        }
    }

    private PrivateKey getPrivateKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(privateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(spec);
        } catch (Exception ex) {
            log.error("[JwtUtils] Error while generating private key", ex);
            throw new InternalErrorException("Error generating key, contact administrator");
        }
    }
}
