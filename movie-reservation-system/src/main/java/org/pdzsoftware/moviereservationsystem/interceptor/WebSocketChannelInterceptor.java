package org.pdzsoftware.moviereservationsystem.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pdzsoftware.moviereservationsystem.dto.event.ClientDisconnectedEvent;
import org.pdzsoftware.moviereservationsystem.exception.custom.UnauthorizedException;
import org.pdzsoftware.moviereservationsystem.util.JwtUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {
    public static final String ORIGIN_ID = "websocket-channel-interceptor";

    public static final String USER_ID = "userId";
    public static final String MOVIE_SESSION_ID = "movieSessionId";

    private final ApplicationEventPublisher eventPublisher;
    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Whenever a front-end client connects to the websocket
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            List<String> movieSessionIdHeaders = accessor.getNativeHeader(MOVIE_SESSION_ID);

            Long userId = getUserIdFromHeaders(authHeaders);
            Long movieSessionId = getMovieSessionIdFromHeaders(movieSessionIdHeaders);

            accessor.getSessionAttributes().put(USER_ID, userId);
            accessor.getSessionAttributes().put(MOVIE_SESSION_ID, movieSessionId);

            log.info("[WebSocketChannelInterceptor] → CONNECTED: session={} movieSessionId={} userId={}",
                    accessor.getSessionId(), movieSessionId, userId);
        }

        return message;
    }

    private Long getUserIdFromHeaders(List<String> authHeaders) {
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String jwt = jwtUtils.parseJwt(authHeaders.get(0));

            if (!jwtUtils.isJwtValid(jwt)) {
                throw new UnauthorizedException("Invalid JWT token");
            }

            return jwtUtils.getUserIdFromToken(jwt);
        } else {
            throw new IllegalArgumentException("No Authorization header in STOMP CONNECT");
        }
    }

    private Long getMovieSessionIdFromHeaders(List<String> movieSessionIdHeaders) {
        if (movieSessionIdHeaders != null && !movieSessionIdHeaders.isEmpty()) {
            return Long.parseLong(movieSessionIdHeaders.get(0));
        } else {
            throw new IllegalArgumentException("No movieSessionId header in STOMP CONNECT");
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel,
                                    boolean sent, Exception ex) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Whenever a front-end client disconnects from the websocket
        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();

            if (sessionAttrs == null) {
                return;
            }

            Long userId = (Long) sessionAttrs.get(USER_ID);
            Long movieSessionId = (Long) sessionAttrs.get(MOVIE_SESSION_ID);

            log.info("[WebSocketChannelInterceptor] ← DISCONNECTED: session={} movieSessionId={} userId={}",
                    sessionId, movieSessionId, userId);

            eventPublisher.publishEvent(new ClientDisconnectedEvent(
                    userId, movieSessionId, ORIGIN_ID
            ));
        }
    }
}
