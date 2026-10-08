package dev.xbhou.javalab.ratelimit;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowLogRateLimiter implements RateLimiter {

    private final int limit;
    private final long windowMillis;
    private final TimeSource clock;
    private final Deque<Long> timestamps = new ArrayDeque<>();

    public SlidingWindowLogRateLimiter(
            int limit,
            long windowMillis,
            TimeSource clock
    ) {
        if (limit <= 0 || windowMillis <= 0) {
            throw new IllegalArgumentException("limit and windowMillis must be > 0");
        }

        this.limit = limit;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    @Override
    public synchronized boolean allow() {
        long now = clock.nowMillis();
        long cutoff = now - windowMillis;

        while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
            timestamps.removeFirst();
        }

        if (timestamps.size() >= limit) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public synchronized int activeRequestCount() {
        return timestamps.size();
    }
}
