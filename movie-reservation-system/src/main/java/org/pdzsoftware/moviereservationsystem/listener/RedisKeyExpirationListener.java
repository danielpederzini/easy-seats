package org.pdzsoftware.moviereservationsystem.listener;

import org.pdzsoftware.moviereservationsystem.dto.event.CacheSeatStatusUpdateEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    public static final String ORIGIN_ID = "redis-expiration-listener";

    private final ApplicationEventPublisher eventPublisher;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      ApplicationEventPublisher eventPublisher) {
        super(listenerContainer);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();

        String[] parts = key.split(":");
        long seatId = Long.parseLong(parts[1]);
        long sessionId = Long.parseLong(parts[2]);

        eventPublisher.publishEvent(new CacheSeatStatusUpdateEvent(
                seatId, sessionId, ORIGIN_ID, false
        ));
    }
}
