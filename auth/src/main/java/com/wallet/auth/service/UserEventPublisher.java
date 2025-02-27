package com.wallet.auth.service;

import com.wallet.auth.model.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class UserEventPublisher {
    private final StringRedisTemplate redisTemplate;
    private static final String STREAM_NAME = "user-events";

    public UserEventPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishUserCreatedEvent(User user) {
        Map<String, String> message = Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "name",user.getName(),
                "eventType","CREATE"
        );
        redisTemplate.opsForStream().add(STREAM_NAME, message);
        System.out.println("User-create event published: " + message);
    }

    public void publishUserDeletedEvent(User user) {
        Map<String, String> message = Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "name",user.getName(),
                "eventType","DELETE"
        );
        redisTemplate.opsForStream().add(STREAM_NAME, message);
        System.out.println("User-delete event published: " + message);
    }
}
