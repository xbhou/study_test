package dev.xbhou.javalab.ratelimit;

public class TokenBucketRateLimiter implements RateLimiter {

    private final double capacity;
    private final double refillTokensPerMillis;
    private final TimeSource clock;

    private double tokens;
    private long lastRefillMillis;

    public TokenBucketRateLimiter(
            int capacity,
            double refillTokensPerSecond,
            TimeSource clock
    ) {
        if (capacity <= 0 || refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException("capacity and refill rate must be > 0");
        }

        this.capacity = capacity;
        this.refillTokensPerMillis = refillTokensPerSecond / 1000.0;
        this.clock = clock;
        this.tokens = capacity;
        this.lastRefillMillis = clock.nowMillis();
    }

    @Override
    public synchronized boolean allow() {
        refill();

        if (tokens < 1.0) {
            return false;
        }

        tokens -= 1.0;
        return true;
    }

    private void refill() {
        long now = clock.nowMillis();
        long elapsed = now - lastRefillMillis;

        if (elapsed <= 0) {
            return;
        }

        tokens = Math.min(
                capacity,
                tokens + elapsed * refillTokensPerMillis
        );

        lastRefillMillis = now;
    }

    public synchronized double availableTokens() {
        refill();
        return tokens;
    }
}
