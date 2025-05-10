package com.team7.bankingapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    @Value("${token.expiration.seconds:900}")
    private long expirationDuration;

    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, PendingEmailChange> emailChangeTokens = new ConcurrentHashMap<>();

    public String generateToken(String email) {
        String token = UUID.randomUUID().toString();
        Instant expiration = Instant.now().plusSeconds(expirationDuration);
        tokenStore.put(token, new TokenEntry(email, expiration));
        return token;
    }

    public String validateToken(String token) {
        TokenEntry entry = tokenStore.get(token);
        if (entry == null || Instant.now().isAfter(entry.expiration)) {
            tokenStore.remove(token);
            return null;
        }
        return entry.email;
    }

    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }

    private static class TokenEntry {
        String email;
        Instant expiration;

        TokenEntry(String email, Instant expiration) {
            this.email = email;
            this.expiration = expiration;
        }
    }

    public String generateEmailChangeToken(String currentEmail, String newEmail) {
        String token = UUID.randomUUID().toString();
        Instant expiration = Instant.now().plusSeconds(expirationDuration);
        emailChangeTokens.put(token, new PendingEmailChange(currentEmail, newEmail, expiration));
        return token;
    }

    public PendingEmailChange validateEmailChangeToken(String token) {
        PendingEmailChange entry = emailChangeTokens.get(token);
        if (entry == null || Instant.now().isAfter(entry.expiration)) {
            emailChangeTokens.remove(token);
            return null;
        }
        return entry;
    }

    public void invalidateEmailChangeToken(String token) {
        emailChangeTokens.remove(token);
    }

    public static class PendingEmailChange {
        public final String currentEmail;
        public final String newEmail;
        public final Instant expiration;

        public PendingEmailChange(String currentEmail, String newEmail, Instant expiration) {
            this.currentEmail = currentEmail;
            this.newEmail = newEmail;
            this.expiration = expiration;
        }
    }
}
