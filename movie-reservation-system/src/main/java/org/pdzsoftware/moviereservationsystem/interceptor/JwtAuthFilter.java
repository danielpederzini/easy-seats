package org.pdzsoftware.moviereservationsystem.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.exception.ErrorResponse;
import org.pdzsoftware.moviereservationsystem.exception.custom.UnauthorizedException;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.pdzsoftware.moviereservationsystem.util.JwtUtils.ROLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/check",
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/auth/logout",
            "/api/movies",
            "/api/webhooks",
            "/api/bookings/validate-qr-code"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = req.getRequestURI();

        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(req, res);
            return;
        }

        String token = parseToken(req);

        if (token != null) {
            try {
                if (!jwtUtils.isJwtValid(token)) {
                    throw new UnauthorizedException("Invalid JWT token");
                }

                Claims claims = jwtUtils.getAllClaimsFromToken(token);

                String username = claims.getSubject();
                String role = claims.get(ROLE, String.class);

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(req, res);
            } catch (UnauthorizedException e) {
                handleException(req, res, e);
            }
        }
    }

    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestURI::startsWith);
    }


    private String parseToken(HttpServletRequest req) {
        try {
            return jwtUtils.parseJwt(req, "accessToken");
        } catch (Exception ex) {
            return null;
        }
    }

    private void handleException(HttpServletRequest req,
                                 HttpServletResponse res,
                                 UnauthorizedException ex) throws IOException {
        res.setStatus(UNAUTHORIZED.value());
        res.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
        res.getWriter().write(errorResponse.toString());
    }

}
