package dev.xbhou.javalab.circuitbreaker;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScriptedService {

    private final Deque<Boolean> outcomes = new ArrayDeque<>();
    private int invocationCount;

    public ScriptedService(boolean... successfulCalls) {
        for (boolean success : successfulCalls) {
            outcomes.addLast(success);
        }
    }

    public String call() {
        invocationCount++;

        boolean success = outcomes.isEmpty() || outcomes.removeFirst();

        if (!success) {
            throw new IllegalStateException(
                    "simulated downstream failure"
            );
        }

        return "downstream success #" + invocationCount;
    }

    public int invocationCount() {
        return invocationCount;
    }
}
