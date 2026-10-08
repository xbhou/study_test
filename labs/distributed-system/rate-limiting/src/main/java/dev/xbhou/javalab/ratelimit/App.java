package dev.xbhou.javalab.ratelimit;

public class App {

    public static void main(String[] args) {
        fixedWindowBoundaryBurst();
        slidingWindowBoundaryProtection();
        tokenBucketBurstAndRefill();
    }

    private static void fixedWindowBoundaryBurst() {
        System.out.println("=== Scenario 1: Fixed Window boundary burst ===");

        ManualClock clock = new ManualClock(990);
        RateLimiter limiter =
                new FixedWindowRateLimiter(5, 1000, clock);

        int beforeBoundary = send(limiter, 5);

        clock.setMillis(1001);

        int afterBoundary = send(limiter, 5);

        System.out.println("allowed at 990ms:  " + beforeBoundary);
        System.out.println("allowed at 1001ms: " + afterBoundary);
        System.out.println("allowed across 11ms: " + (beforeBoundary + afterBoundary));
        System.out.println();
    }

    private static void slidingWindowBoundaryProtection() {
        System.out.println("=== Scenario 2: Sliding Window blocks boundary burst ===");

        ManualClock clock = new ManualClock(990);
        SlidingWindowLogRateLimiter limiter =
                new SlidingWindowLogRateLimiter(5, 1000, clock);

        int beforeBoundary = send(limiter, 5);

        clock.setMillis(1001);

        int immediatelyAfterBoundary = send(limiter, 5);

        clock.setMillis(1991);

        int afterOldRequestsExpire = send(limiter, 5);

        System.out.println("allowed at 990ms:  " + beforeBoundary);
        System.out.println("allowed at 1001ms: " + immediatelyAfterBoundary);
        System.out.println("allowed at 1991ms: " + afterOldRequestsExpire);
        System.out.println();
    }

    private static void tokenBucketBurstAndRefill() {
        System.out.println("=== Scenario 3: Token Bucket burst and refill ===");

        ManualClock clock = new ManualClock(0);
        TokenBucketRateLimiter limiter =
                new TokenBucketRateLimiter(5, 5.0, clock);

        int initialBurst = send(limiter, 6);

        clock.advanceMillis(400);

        int afterRefill = send(limiter, 3);

        System.out.println("initial 6 requests allowed: " + initialBurst);
        System.out.println("requests allowed after 400ms refill: " + afterRefill);
        System.out.printf("tokens remaining: %.2f%n", limiter.availableTokens());
        System.out.println();
    }

    private static int send(RateLimiter limiter, int requests) {
        int allowed = 0;

        for (int i = 0; i < requests; i++) {
            if (limiter.allow()) {
                allowed++;
            }
        }

        return allowed;
    }
}
