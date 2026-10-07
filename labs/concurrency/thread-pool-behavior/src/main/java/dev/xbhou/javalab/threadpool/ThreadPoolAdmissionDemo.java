package dev.xbhou.javalab.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolAdmissionDemo {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int QUEUE_CAPACITY = 2;
    private static final int TASK_COUNT = 8;

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger rejectedCount = new AtomicInteger();
        CountDownLatch releaseGate = new CountDownLatch(1);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new NamedThreadFactory(),
                (task, pool) -> {
                    rejectedCount.incrementAndGet();
                    throw new RejectedExecutionException("Task rejected because the pool is saturated");
                }
        );

        for (int taskId = 1; taskId <= TASK_COUNT; taskId++) {
            try {
                executor.execute(new BlockingTask(taskId, releaseGate));
                printMetrics("accepted task " + taskId, executor, rejectedCount.get());
            } catch (RejectedExecutionException e) {
                printMetrics("rejected task " + taskId, executor, rejectedCount.get());
            }
        }

        System.out.println();
        System.out.println("All tasks submitted. Releasing blocked workers...");
        releaseGate.countDown();

        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        System.out.println();
        printMetrics("final", executor, rejectedCount.get());
    }

    private static void printMetrics(String stage,
                                     ThreadPoolExecutor executor,
                                     int rejectedCount) {
        System.out.printf(
                "%-18s pool=%d active=%d queued=%d completed=%d rejected=%d%n",
                stage,
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount(),
                rejectedCount
        );
    }

    private record BlockingTask(int taskId, CountDownLatch releaseGate) implements Runnable {

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.printf("task-%d started on %s%n", taskId, threadName);

            try {
                releaseGate.await();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.printf("task-%d finished on %s%n", taskId, threadName);
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger sequence = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable task) {
            return new Thread(task, "lab-worker-" + sequence.getAndIncrement());
        }
    }
}
