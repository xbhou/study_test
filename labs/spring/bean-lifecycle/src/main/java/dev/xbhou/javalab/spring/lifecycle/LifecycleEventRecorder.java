package dev.xbhou.javalab.spring.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LifecycleEventRecorder {

    private final AtomicInteger sequence = new AtomicInteger();
    private final List<String> events = new ArrayList<>();

    public synchronized void record(String event) {
        int index = sequence.incrementAndGet();
        events.add(event);

        System.out.printf("%02d %s%n", index, event);
    }

    public synchronized List<String> snapshot() {
        return List.copyOf(events);
    }

    public synchronized void reset() {
        events.clear();
        sequence.set(0);
    }
}
