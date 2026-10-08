package dev.xbhou.javalab.threaddump;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadDumpLab {

    private static final Object BLOCKED_LOCK = new Object();
    private static final Object DEADLOCK_LOCK_A = new Object();
    private static final Object DEADLOCK_LOCK_B = new Object();

    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        long holdSeconds = args.length == 0
                ? 60
                : Long.parseLong(args[0]);

        if (holdSeconds < 5) {
            throw new IllegalArgumentException(
                    "holdSeconds must be >= 5"
            );
        }

        System.out.println("PID=" + ProcessHandle.current().pid());
        System.out.println(
                "Inspect with: jcmd "
                        + ProcessHandle.current().pid()
                        + " Thread.print -l"
        );
        System.out.println();

        Thread runnable = startRunnableThread();
        Thread holder = startLockHolder();
        Thread blocked = startBlockedWaiter(holder);
        Thread waiting = startWaitingThread();
        Thread timedWaiting = startTimedWaitingThread();
        List<Thread> deadlocked = startDeadlock();
        ThreadPoolExecutor executor = startSaturatedThreadPool();

        Thread.sleep(1000);

        System.out.println("=== Java-level state snapshot ===");
        printState(runnable);
        printState(holder);
        printState(blocked);
        printState(waiting);
        printState(timedWaiting);
        deadlocked.forEach(ThreadDumpLab::printState);

        System.out.printf(
                "%s state=%s%n",
                executor.getThreadFactory().getClass().getSimpleName(),
                "see lab-pool-worker-1 in thread dump"
        );

        System.out.printf(
                "threadPool poolSize=%d active=%d queueSize=%d%n",
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size()
        );

        System.out.println();
        System.out.println(
                "Expected highlights: RUNNABLE, BLOCKED, WAITING, "
                        + "TIMED_WAITING, Java-level deadlock, pool queue backlog"
        );
        System.out.println(
                "Holding process for " + holdSeconds + " seconds..."
        );

        Thread.sleep(Duration.ofSeconds(holdSeconds).toMillis());

        RUNNING.set(false);
        executor.shutdownNow();

        System.out.println("main finished");
    }

    private static Thread startRunnableThread() {
        return startDaemon(
                "lab-runnable-cpu",
                ThreadDumpLab::cpuLoop
        );
    }

    private static void cpuLoop() {
        long value = 0x9E3779B97F4A7C15L;

        while (RUNNING.get()) {
            value ^= value << 13;
            value ^= value >>> 7;
            value ^= value << 17;
        }

        if (value == 0) {
            System.out.println("unreachable");
        }
    }

    private static Thread startLockHolder() throws InterruptedException {
        CountDownLatch acquired = new CountDownLatch(1);

        Thread thread = startDaemon(
                "lab-lock-holder",
                () -> {
                    synchronized (BLOCKED_LOCK) {
                        acquired.countDown();

                        while (RUNNING.get()) {
                            sleepQuietly(1000);
                        }
                    }
                }
        );

        if (!acquired.await(1, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "lock holder did not acquire monitor"
            );
        }

        return thread;
    }

    private static Thread startBlockedWaiter(Thread holder) {
        if (!holder.isAlive()) {
            throw new IllegalStateException("holder must be alive");
        }

        return startDaemon(
                "lab-blocked-waiter",
                () -> {
                    synchronized (BLOCKED_LOCK) {
                        System.out.println(
                                "blocked waiter unexpectedly acquired monitor"
                        );
                    }
                }
        );
    }

    private static Thread startWaitingThread() {
        CountDownLatch neverReleased = new CountDownLatch(1);

        return startDaemon(
                "lab-waiting-latch",
                () -> {
                    try {
                        neverReleased.await();
                    } catch (InterruptedException error) {
                        Thread.currentThread().interrupt();
                    }
                }
        );
    }

    private static Thread startTimedWaitingThread() {
        return startDaemon(
                "lab-timed-sleeper",
                () -> sleepQuietly(120_000)
        );
    }

    private static List<Thread> startDeadlock()
            throws InterruptedException {
        CountDownLatch firstLocksAcquired = new CountDownLatch(2);

        Thread a = startDaemon(
                "lab-deadlock-a",
                () -> {
                    synchronized (DEADLOCK_LOCK_A) {
                        firstLocksAcquired.countDown();
                        awaitQuietly(firstLocksAcquired);

                        synchronized (DEADLOCK_LOCK_B) {
                            System.out.println("unreachable A");
                        }
                    }
                }
        );

        Thread b = startDaemon(
                "lab-deadlock-b",
                () -> {
                    synchronized (DEADLOCK_LOCK_B) {
                        firstLocksAcquired.countDown();
                        awaitQuietly(firstLocksAcquired);

                        synchronized (DEADLOCK_LOCK_A) {
                            System.out.println("unreachable B");
                        }
                    }
                }
        );

        if (!firstLocksAcquired.await(1, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "deadlock threads did not acquire first locks"
            );
        }

        return List.of(a, b);
    }

    private static ThreadPoolExecutor startSaturatedThreadPool() {
        CountDownLatch workerGate = new CountDownLatch(1);

        ThreadFactory factory = new NamedDaemonThreadFactory(
                "lab-pool-worker-"
        );

        RejectedExecutionHandler reject =
                new ThreadPoolExecutor.AbortPolicy();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(4),
                factory,
                reject
        );

        executor.execute(() -> {
            try {
                workerGate.await();
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        });

        for (int i = 1; i <= 4; i++) {
            int taskId = i;
            executor.execute(
                    () -> System.out.println(
                            "queued task executed: " + taskId
                    )
            );
        }

        return executor;
    }

    private static Thread startDaemon(
            String name,
            Runnable action
    ) {
        Thread thread = new Thread(action, name);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void printState(Thread thread) {
        System.out.printf(
                "%-24s state=%s%n",
                thread.getName(),
                thread.getState()
        );
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private static class NamedDaemonThreadFactory
            implements ThreadFactory {

        private final String prefix;
        private final AtomicInteger sequence = new AtomicInteger();

        private NamedDaemonThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(
                    runnable,
                    prefix + sequence.incrementAndGet()
            );
            thread.setDaemon(true);
            return thread;
        }
    }
}
