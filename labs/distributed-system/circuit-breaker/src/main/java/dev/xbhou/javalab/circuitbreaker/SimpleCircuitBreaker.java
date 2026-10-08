package dev.xbhou.javalab.circuitbreaker;

import java.time.Duration;
import java.util.concurrent.Callable;

public class SimpleCircuitBreaker {

    private final int failureThreshold;
    private final long openDurationMillis;
    private final TimeSource clock;

    private CircuitState state = CircuitState.CLOSED;
    private int consecutiveFailures;
    private long openedAtMillis;
    private boolean halfOpenProbeInFlight;

    public SimpleCircuitBreaker(
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

    public <T> T execute(Callable<T> operation) throws Exception {
        CircuitState callState = beforeCall();

        try {
            T value = operation.call();
            onSuccess(callState);
            return value;
        } catch (Exception error) {
            onFailure(callState);
            throw error;
        }
    }

    private synchronized CircuitState beforeCall() {
        if (state == CircuitState.OPEN) {
            long elapsed = clock.nowMillis() - openedAtMillis;

            if (elapsed < openDurationMillis) {
                throw new CircuitOpenException(
                        "circuit is OPEN; retry later"
                );
            }

            state = CircuitState.HALF_OPEN;
            halfOpenProbeInFlight = false;
            System.out.println("state transition: OPEN -> HALF_OPEN");
        }

        if (state == CircuitState.HALF_OPEN) {
            if (halfOpenProbeInFlight) {
                throw new CircuitOpenException(
                        "HALF_OPEN probe already in flight"
                );
            }

            halfOpenProbeInFlight = true;
        }

        return state;
    }

    private synchronized void onSuccess(CircuitState callState) {
        if (callState == CircuitState.HALF_OPEN) {
            state = CircuitState.CLOSED;
            halfOpenProbeInFlight = false;
            consecutiveFailures = 0;
            System.out.println("state transition: HALF_OPEN -> CLOSED");
            return;
        }

        consecutiveFailures = 0;
    }

    private synchronized void onFailure(CircuitState callState) {
        if (callState == CircuitState.HALF_OPEN) {
            halfOpenProbeInFlight = false;
            open();
            System.out.println("state transition: HALF_OPEN -> OPEN");
            return;
        }

        consecutiveFailures++;

        if (consecutiveFailures >= failureThreshold) {
            open();
            System.out.println("state transition: CLOSED -> OPEN");
        }
    }

    private void open() {
        state = CircuitState.OPEN;
        openedAtMillis = clock.nowMillis();
        consecutiveFailures = 0;
    }

    public synchronized CircuitState state() {
        return state;
    }

    public synchronized int consecutiveFailures() {
        return consecutiveFailures;
    }
}
