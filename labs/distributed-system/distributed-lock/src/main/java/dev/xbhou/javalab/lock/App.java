package dev.xbhou.javalab.lock;

import java.time.Duration;

public class App {

    private static final String RESOURCE = "inventory:sku-1001";

    public static void main(String[] args) throws Exception {
        unsafeUnlockScenario();
        safeUnlockScenario();
        leaseRenewalScenario();
        fencingTokenScenario();
    }

    private static void unsafeUnlockScenario() throws Exception {
        System.out.println("=== Scenario 1: unsafe unlock deletes another owner's lock ===");

        InMemoryDistributedLock lock = new InMemoryDistributedLock();

        LockHandle ownerA = require(
                lock.acquire(RESOURCE, "owner-A", Duration.ofMillis(100))
        );

        Thread.sleep(130);

        LockHandle ownerB = require(
                lock.acquire(RESOURCE, "owner-B", Duration.ofMillis(500))
        );

        System.out.println("current owner after B acquires: "
                + lock.currentOwner(RESOURCE).orElse("none"));

        System.out.println("A performs unsafe DEL...");
        lock.unsafeRelease(RESOURCE);

        boolean ownerCAcquired = lock.acquire(
                RESOURCE,
                "owner-C",
                Duration.ofMillis(500)
        ).isPresent();

        System.out.println("C acquired while B should still own lock: "
                + ownerCAcquired);

        // Keep compiler from treating these as unused teaching variables.
        System.out.println("A fencing token=" + ownerA.fencingToken()
                + ", B fencing token=" + ownerB.fencingToken());
        System.out.println();
    }

    private static void safeUnlockScenario() throws Exception {
        System.out.println("=== Scenario 2: owner token prevents deleting a newer lock ===");

        InMemoryDistributedLock lock = new InMemoryDistributedLock();

        LockHandle ownerA = require(
                lock.acquire(RESOURCE, "owner-A", Duration.ofMillis(100))
        );

        Thread.sleep(130);

        LockHandle ownerB = require(
                lock.acquire(RESOURCE, "owner-B", Duration.ofMillis(500))
        );

        boolean aReleased = lock.release(ownerA);
        boolean ownerCAcquired = lock.acquire(
                RESOURCE,
                "owner-C",
                Duration.ofMillis(500)
        ).isPresent();

        System.out.println("A late release accepted: " + aReleased);
        System.out.println("current owner: "
                + lock.currentOwner(RESOURCE).orElse("none"));
        System.out.println("C acquired while B owns lock: "
                + ownerCAcquired);
        System.out.println("B fencing token=" + ownerB.fencingToken());
        System.out.println();
    }

    private static void leaseRenewalScenario() throws Exception {
        System.out.println("=== Scenario 3: renewal keeps ownership during long work ===");

        InMemoryDistributedLock lock = new InMemoryDistributedLock();

        LockHandle ownerA = require(
                lock.acquire(RESOURCE, "owner-A", Duration.ofMillis(120))
        );

        for (int i = 1; i <= 3; i++) {
            Thread.sleep(70);
            boolean renewed = lock.renew(
                    ownerA,
                    Duration.ofMillis(120)
            );

            System.out.println("renew " + i + " accepted: " + renewed);
        }

        boolean ownerBAcquired = lock.acquire(
                RESOURCE,
                "owner-B",
                Duration.ofMillis(200)
        ).isPresent();

        System.out.println("B acquired while A is renewed: "
                + ownerBAcquired);

        boolean released = lock.release(ownerA);
        System.out.println("A release accepted: " + released);
        System.out.println();
    }

    private static void fencingTokenScenario() throws Exception {
        System.out.println("=== Scenario 4: fencing token rejects stale write ===");

        InMemoryDistributedLock lock = new InMemoryDistributedLock();
        FencedResource resource = new FencedResource();

        LockHandle ownerA = require(
                lock.acquire(RESOURCE, "owner-A", Duration.ofMillis(100))
        );

        Thread.sleep(130);

        LockHandle ownerB = require(
                lock.acquire(RESOURCE, "owner-B", Duration.ofMillis(500))
        );

        boolean bWrite = resource.write(
                ownerB.fencingToken(),
                "value-from-B"
        );

        boolean staleAWrite = resource.write(
                ownerA.fencingToken(),
                "late-value-from-A"
        );

        System.out.println("B write accepted: " + bWrite);
        System.out.println("A stale write accepted: " + staleAWrite);
        System.out.println("final resource value: " + resource.value());
        System.out.println();
    }

    private static LockHandle require(java.util.Optional<LockHandle> handle) {
        return handle.orElseThrow(
                () -> new IllegalStateException("expected lock acquisition")
        );
    }
}
