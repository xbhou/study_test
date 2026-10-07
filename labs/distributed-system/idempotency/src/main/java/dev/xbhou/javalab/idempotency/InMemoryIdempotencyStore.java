package dev.xbhou.javalab.idempotency;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore {

    private final ConcurrentHashMap<String, Entry> records = new ConcurrentHashMap<>();

    public BeginDecision begin(String key, String fingerprint) {
        Entry candidate = new Entry(fingerprint);
        Entry existing = records.putIfAbsent(key, candidate);

        if (existing == null) {
            return new BeginDecision(Action.START, candidate);
        }

        if (!existing.fingerprint.equals(fingerprint)) {
            return new BeginDecision(Action.CONFLICT, existing);
        }

        if (existing.status == RecordStatus.SUCCESS) {
            return new BeginDecision(Action.REPLAY_SUCCESS, existing);
        }

        return new BeginDecision(Action.IN_PROGRESS, existing);
    }

    public void completeSuccess(String key, PaymentResult result) {
        Entry entry = records.get(key);
        if (entry == null) {
            throw new IllegalStateException("idempotency record missing for key " + key);
        }

        entry.result = result;
        entry.status = RecordStatus.SUCCESS;
    }

    public enum Action {
        START,
        REPLAY_SUCCESS,
        IN_PROGRESS,
        CONFLICT
    }

    private enum RecordStatus {
        PROCESSING,
        SUCCESS
    }

    public record BeginDecision(Action action, Entry entry) {
    }

    public static class Entry {
        private final String fingerprint;
        private volatile RecordStatus status = RecordStatus.PROCESSING;
        private volatile PaymentResult result;

        private Entry(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public PaymentResult result() {
            return result;
        }
    }
}
