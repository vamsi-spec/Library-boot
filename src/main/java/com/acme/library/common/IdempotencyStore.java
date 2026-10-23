package com.acme.library.common;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class IdempotencyStore {
    public enum Status {
        PROCEED,
        REPLAY,
        IN_PROGRESS,
        MISMATCH
    }

    public record Claim(Status status, Long resultId) {}

    private record Entry(String fingerprint, Long resultId, Instant expiresAt) {}

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    private final Clock clock;
    private final Duration ttl;

    public IdempotencyStore(Clock clock, Duration ttl) {
        this.clock = clock;
        this.ttl = ttl;
    }

    public Claim claim(String key, String fingerprint) {
        Instant now = Instant.now(clock);
        AtomicReference<Claim> outcome = new AtomicReference<>();

        entries.compute(key, (k,existing) -> {
            if(existing == null || existing.expiresAt().isBefore(now)) {
                outcome.set(new Claim(Status.PROCEED, null));
                return new Entry(fingerprint, null, now.plus(ttl));
            }

            if(!existing.fingerprint().equals(fingerprint)) {
                outcome.set(new Claim(Status.MISMATCH, null));
            }

            else if(existing.resultId() == null) {
                outcome.set(new Claim(Status.IN_PROGRESS, null));
            }
            else {
                outcome.set(new Claim(Status.REPLAY, existing.resultId()));
            }

            return existing;
        });
        return outcome.get();
    }

    public void complete(String key, long resultId) {
        entries.computeIfPresent(key, (k,existing) -> new Entry(existing.fingerprint(), resultId, existing.expiresAt()));
    }

    public void release(String key) {
        entries.remove(key);
    }

}
