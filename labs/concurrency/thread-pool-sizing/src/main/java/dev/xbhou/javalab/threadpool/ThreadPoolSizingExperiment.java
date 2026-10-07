package dev.xbhou.javalab.threadpool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ThreadPoolSizingExperiment {

    private static final int TASK_COUNT = 24;
    private static final Duration TASK_DURATION = Duration.ofMillis(80);

    private static volatile long blackhole;

    public static void main(String[] args) throws Exception {
        int detectedProcessors = Runtime.getRuntime().availableProcessors();
        int effectiveProcessors = Math.min(detectedProcessors, 8);

        System.out.println("Detected processors: " + detectedProcessors);
        System.out.println("Demo processor cap: " + effectiveProcessors);
        System.out.println();

        for (Workload workload : Workload.values()) {
            System.out.println(workload);
            for (int poolSize : candidatePoolSizes(effectiveProcessors)) {
                Result result = run(workload, poolSize);
                System.out.printf(
                        "pool=%-2d elapsed=%4d ms throughput=%6.2f tasks/s%n",
                        poolSize,
                        result.elapsedMillis(),
                        result.throughputPerSecond()
                );
            }
            System.out.println();
        }

        System.out.println("blackhole=" + blackhole);
    }

    private static Set<Integer> candidatePoolSizes(int processors) {
        Set<Integer> sizes = new LinkedHashSet<>();
        sizes.add(1);
        sizes.add(Math.max(2, processors));
        sizes.add(Math.min(16, Math.max(2, processors * 2)));
        return sizes;
    }

    private static Result run(Workload workload, int poolSize) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        long start = System.nanoTime();

        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < TASK_COUNT; i++) {
                tasks.add(() -> {
                    workload.run(TASK_DURATION);
                    return null;
                });
            }

            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        long elapsedNanos = System.nanoTime() - start;
        return new Result(TASK_COUNT, elapsedNanos);
    }

    private enum Workload {
        CPU_BOUND {
            @Override
            void run(Duration duration) {
                long deadline = System.nanoTime() + duration.toNanos();
                long value = 0x9E3779B97F4A7C15L;

                while (System.nanoTime() < deadline) {
                    value ^= value << 13;
                    value ^= value >>> 7;
                    value ^= value << 17;
                }

                blackhole ^= value;
            }
        },
        IO_BOUND {
            @Override
            void run(Duration duration) throws InterruptedException {
                Thread.sleep(duration.toMillis());
            }
        };

        abstract void run(Duration duration) throws Exception;
    }

    private record Result(int taskCount, long elapsedNanos) {

        long elapsedMillis() {
            return TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        }

        double throughputPerSecond() {
            return taskCount / (elapsedNanos / 1_000_000_000.0);
        }
    }
}
