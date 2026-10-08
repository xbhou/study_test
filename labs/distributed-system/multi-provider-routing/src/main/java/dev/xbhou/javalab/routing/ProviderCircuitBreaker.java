package dev.xbhou.javalab.routing;

import java.time.Duration;

public class ProviderCircuitBreaker {

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final int failureThreshold;
    private final long openDurationMillis;
    private final TimeSource clock;

    private State state = State.CLOSED;
    private int consecutiveFailures;
    private long openedAtMillis;
    private boolean halfOpenProbeInFlight;

    public ProviderCircuitBreaker(
            int failureThreshold,
            Duration openDuration,
            TimeSource clock
    ) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be > 0");
        }
        if (openDuration.isZero() || openDuration.isNegative()) {
            throw new IllegalArgumentException("openDuration must be > 0");
        }

        this.failureThreshold = failureThreshold;
        this.openDurationMillis = openDuration.toMillis();
        this.clock = clock;
    }

    public synchronized Permit acquirePermission() {
        if (state == State.OPEN) {
            long elapsed = clock.nowMillis() - openedAtMillis;

            if (elapsed < openDurationMillis) {
                return Permit.denied();
            }

            state = State.HALF_OPEN;
            halfOpenProbeInFlight = false;
        }

        if (state == State.HALF_OPEN) {
            if (halfOpenProbeInFlight) {
                return Permit.denied();
            }

            halfOpenProbeInFlight = true;
            return Permit.allowed(true);
        }

        return Permit.allowed(false);
    }

    public synchronized void onSuccess(Permit permit) {
        if (!permit.allowed()) {
            return;
        }

        if (permit.halfOpenProbe()) {
            state = State.CLOSED;
            halfOpenProbeInFlight = false;
        }

        consecutiveFailures = 0;
    }

    public synchronized void onFailure(Permit permit) {
        if (!permit.allowed()) {
            return;
        }

        if (permit.halfOpenProbe()) {
            halfOpenProbeInFlight = false;
            open();
            return;
        }

        consecutiveFailures++;

        if (consecutiveFailures >= failureThreshold) {
            open();
        }
    }

    private void open() {
        state = State.OPEN;
        openedAtMillis = clock.nowMillis();
        consecutiveFailures = 0;
    }

    public synchronized State state() {
        return state;
    }

    public record Permit(boolean allowed, boolean halfOpenProbe) {

        public static Permit denied() {
            return new Permit(false, false);
        }

        public static Permit allowed(boolean halfOpenProbe) {
            return new Permit(true, halfOpenProbe);
        }
    }
}
