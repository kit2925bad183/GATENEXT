package com.gatenest.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimiterService {

    private final Map<String, ConcurrentLinkedDeque<Long>> requestLogs = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        long now = Instant.now().toEpochMilli();
        long windowMillis = windowSeconds * 1000L;

        ConcurrentLinkedDeque<Long> timestamps = requestLogs.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // Purge expired timestamps
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > windowMillis) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }

        return false;
    }
}
