
package com.apishield.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(Long clientId, int limit) {
        long currentWindow = System.currentTimeMillis() / 60_000;

        String key = "rate-limit:" + clientId + ":" + currentWindow;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            throw new IllegalStateException(
                    "Could not increment rate-limit counter in Redis"
            );
        }

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(2));
        }

        return count <= limit;
    }
}
