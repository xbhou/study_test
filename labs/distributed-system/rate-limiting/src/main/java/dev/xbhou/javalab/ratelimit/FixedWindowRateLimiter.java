package dev.xbhou.javalab.ratelimit;

public class FixedWindowRateLimiter implements RateLimiter {

    private final int limit;
    private final long windowMillis;
    private final TimeSource clock;

    private long currentWindow;
    private int count;

    public FixedWindowRateLimiter(
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
        this.currentWindow = clock.nowMillis() / windowMillis;
    }

    @Override
    public synchronized boolean allow() {
        long window = clock.nowMillis() / windowMillis;

        if (window != currentWindow) {
            currentWindow = window;
            count = 0;
        }

        if (count >= limit) {
            return false;
        }

        count++;
        return true;
    }
}
