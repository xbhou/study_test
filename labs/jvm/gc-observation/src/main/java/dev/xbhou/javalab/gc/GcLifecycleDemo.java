package dev.xbhou.javalab.gc;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

public class GcLifecycleDemo {

    private static final int KiB = 1024;
    private static final int MiB = 1024 * 1024;

    public static void main(String[] args) throws Exception {
        List<byte[]> retained = new ArrayList<>();

        printHeapPools("before allocation");

        for (int round = 1; round <= 12; round++) {
            allocateShortLivedObjects();

            retained.add(new byte[512 * KiB]);

            System.out.printf(
                    "round=%d retained=%.1f MiB%n",
                    round,
                    retained.size() * 0.5
            );

            Thread.sleep(20);
        }

        printHeapPools("after young GC pressure");

        System.out.println("dropping half of retained objects...");
        retained.subList(0, retained.size() / 2).clear();

        System.out.println(
                "requesting System.gc() for teaching observation..."
        );

        System.gc();
        Thread.sleep(200);

        printHeapPools("after System.gc()");

        System.out.printf(
                "done; still retained=%.1f MiB%n",
                retained.size() * 0.5
        );
    }

    private static void allocateShortLivedObjects() {
        for (int i = 0; i < 80; i++) {
            byte[] temporary = new byte[128 * KiB];
            temporary[0] = 1;
        }
    }

    private static void printHeapPools(String phase) {
        System.out.println();
        System.out.println(
                "--- heap pools: " + phase + " ---"
        );

        ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .forEach(GcLifecycleDemo::printPool);

        System.out.println();
    }

    private static void printPool(MemoryPoolMXBean pool) {
        if (pool.getUsage() == null) {
            return;
        }

        System.out.printf(
                "%s used=%.2f MiB committed=%.2f MiB max=%s%n",
                pool.getName(),
                toMiB(pool.getUsage().getUsed()),
                toMiB(pool.getUsage().getCommitted()),
                pool.getUsage().getMax() < 0
                        ? "undefined"
                        : String.format(
                                "%.2f MiB",
                                toMiB(pool.getUsage().getMax())
                        )
        );
    }

    private static double toMiB(long bytes) {
        return bytes / (double) MiB;
    }
}
