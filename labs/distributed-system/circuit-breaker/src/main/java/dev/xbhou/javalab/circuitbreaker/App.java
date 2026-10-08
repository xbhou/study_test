package dev.xbhou.javalab.circuitbreaker;

import java.time.Duration;

public class App {

    public static void main(String[] args) throws Exception {
        failureOpensCircuit();
        successfulProbeClosesCircuit();
        failedProbeReopensCircuit();
    }

    private static void failureOpensCircuit() {
        System.out.println("=== Scenario 1: failures open the circuit ===");

        ManualClock clock = new ManualClock(0);
        SimpleCircuitBreaker breaker =
                new SimpleCircuitBreaker(
                        3,
                        Duration.ofMillis(1000),
                        clock
                );

        ScriptedService service =
                new ScriptedService(false, false, false, true);

        for (int i = 1; i <= 3; i++) {
            try {
                breaker.execute(service::call);
            } catch (Exception error) {
                System.out.printf(
                        "call %d failed, state=%s%n",
                        i,
                        breaker.state()
                );
            }
        }

        int beforeFastFail = service.invocationCount();

        try {
            breaker.execute(service::call);
        } catch (CircuitOpenException error) {
            System.out.println("4th call failed fast: " + error.getMessage());
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }

        System.out.println("state: " + breaker.state());
        System.out.println(
                "downstream invoked during fast fail: "
                        + (service.invocationCount() - beforeFastFail)
        );
        System.out.println();
    }

    private static void successfulProbeClosesCircuit() throws Exception {
        System.out.println("=== Scenario 2: successful HALF_OPEN probe closes circuit ===");

        ManualClock clock = new ManualClock(0);
        SimpleCircuitBreaker breaker =
                new SimpleCircuitBreaker(
                        2,
                        Duration.ofMillis(1000),
                        clock
                );

        ScriptedService service =
                new ScriptedService(false, false, true, true);

        failTwice(breaker, service);

        System.out.println("state before time advance: " + breaker.state());

        clock.advanceMillis(1000);

        String probeResult = breaker.execute(service::call);

        System.out.println("probe result: " + probeResult);
        System.out.println("state after probe: " + breaker.state());

        String normalResult = breaker.execute(service::call);

        System.out.println("normal result after recovery: " + normalResult);
        System.out.println();
    }

    private static void failedProbeReopensCircuit() {
        System.out.println("=== Scenario 3: failed HALF_OPEN probe reopens circuit ===");

        ManualClock clock = new ManualClock(0);
        SimpleCircuitBreaker breaker =
                new SimpleCircuitBreaker(
                        2,
                        Duration.ofMillis(1000),
                        clock
                );

        ScriptedService service =
                new ScriptedService(false, false, false, true);

        failTwice(breaker, service);

        clock.advanceMillis(1000);

        try {
            breaker.execute(service::call);
        } catch (Exception error) {
            System.out.println("probe failed: " + error.getMessage());
        }

        System.out.println("state after failed probe: " + breaker.state());

        int beforeFastFail = service.invocationCount();

        try {
            breaker.execute(service::call);
        } catch (CircuitOpenException error) {
            System.out.println("next call failed fast: " + error.getMessage());
        } catch (Exception impossible) {
            throw new IllegalStateException(impossible);
        }

        System.out.println(
                "downstream invoked during fast fail: "
                        + (service.invocationCount() - beforeFastFail)
        );
        System.out.println();
    }

    private static void failTwice(
            SimpleCircuitBreaker breaker,
            ScriptedService service
    ) {
        for (int i = 0; i < 2; i++) {
            try {
                breaker.execute(service::call);
            } catch (Exception ignored) {
                // Expected in this teaching scenario.
            }
        }
    }
}
