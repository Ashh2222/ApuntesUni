package com.university.website.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = 15 * 60 * 1000L; // 15 minutos

    private static class AttemptInfo {
        int attempts;
        Instant firstAttempt;
        Instant blockedUntil;

        AttemptInfo(int attempts, Instant firstAttempt) {
            this.attempts = attempts;
            this.firstAttempt = firstAttempt;
        }
    }

    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        if (key != null) {
            attemptsCache.remove(key.trim().toLowerCase());
        }
    }

    public void loginFailed(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        String cleanKey = key.trim().toLowerCase();
        Instant now = Instant.now();
        attemptsCache.compute(cleanKey, (k, info) -> {
            if (info == null) {
                return new AttemptInfo(1, now);
            }
            if (info.blockedUntil != null && now.isAfter(info.blockedUntil)) {
                return new AttemptInfo(1, now);
            }
            info.attempts++;
            if (info.attempts >= MAX_ATTEMPTS) {
                info.blockedUntil = now.plusMillis(BLOCK_DURATION_MS);
            }
            return info;
        });
    }

    public boolean isBlocked(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        AttemptInfo info = attemptsCache.get(key.trim().toLowerCase());
        if (info == null) {
            return false;
        }
        if (info.blockedUntil != null) {
            if (Instant.now().isBefore(info.blockedUntil)) {
                return true;
            } else {
                attemptsCache.remove(key.trim().toLowerCase());
                return false;
            }
        }
        return false;
    }
}
