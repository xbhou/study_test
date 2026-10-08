package dev.xbhou.javalab.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryDistributedLock {

    private final ConcurrentHashMap<String, LockRecord> locks = new ConcurrentHashMap<>();
    private final AtomicLong fencingSequence = new AtomicLong();

    public Optional<LockHandle> acquire(
            String key,
            String ownerToken,
            Duration lease
    ) {
        long now = System.nanoTime();
        long expiresAt = now + lease.toNanos();

        AtomicReference<LockHandle> acquired = new AtomicReference<>();

        locks.compute(key, (ignored, current) -> {
            if (current == null || current.expired(now)) {
                long fencingToken = fencingSequence.incrementAndGet();
                acquired.set(new LockHandle(key, ownerToken, fencingToken));
                return new LockRecord(ownerToken, fencingToken, expiresAt);
            }

            return current;
        });

        return Optional.ofNullable(acquired.get());
    }

    public boolean release(LockHandle handle) {
        AtomicBoolean released = new AtomicBoolean(false);

        locks.computeIfPresent(handle.key(), (ignored, current) -> {
            if (current.matches(handle)) {
                released.set(true);
                return null;
            }

            return current;
        });

        return released.get();
    }

    public void unsafeRelease(String key) {
        locks.remove(key);
    }

    public boolean renew(LockHandle handle, Duration lease) {
        long now = System.nanoTime();
        AtomicBoolean renewed = new AtomicBoolean(false);

        locks.computeIfPresent(handle.key(), (ignored, current) -> {
            if (current.matches(handle) && !current.expired(now)) {
                renewed.set(true);
                return current.renewed(now + lease.toNanos());
            }

            return current;
        });

        return renewed.get();
    }

    public Optional<String> currentOwner(String key) {
        long now = System.nanoTime();
        LockRecord record = locks.get(key);

        if (record == null || record.expired(now)) {
            return Optional.empty();
        }

        return Optional.of(record.ownerToken());
    }

    private record LockRecord(
            String ownerToken,
            long fencingToken,
            long expiresAtNanos
    ) {
        boolean expired(long now) {
            return now >= expiresAtNanos;
        }

        boolean matches(LockHandle handle) {
            return ownerToken.equals(handle.ownerToken())
                    && fencingToken == handle.fencingToken();
        }

        LockRecord renewed(long newExpiresAtNanos) {
            return new LockRecord(
                    ownerToken,
                    fencingToken,
                    newExpiresAtNanos
            );
        }
    }
}
