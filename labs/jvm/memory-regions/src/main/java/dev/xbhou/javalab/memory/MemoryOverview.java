package dev.xbhou.javalab.memory;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

public class MemoryOverview {

    public static void main(String[] args) {
        Object heapObject = new byte[1024 * 1024];
        int localValue = 42;

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();

        System.out.println("=== Heap ===");
        printUsage(heap);

        System.out.println();
        System.out.println("=== Metaspace ===");

        ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getName().contains("Metaspace"))
                .map(MemoryPoolMXBean::getUsage)
                .forEach(MemoryOverview::printUsage);

        ClassLoadingMXBean classLoading =
                ManagementFactory.getClassLoadingMXBean();

        System.out.println();
        System.out.println("=== Class Loading ===");
        System.out.println(
                "currently loaded classes=" + classLoading.getLoadedClassCount()
        );
        System.out.println(
                "total loaded classes=" + classLoading.getTotalLoadedClassCount()
        );

        System.out.println();
        System.out.println("=== Current Thread Stack ===");
        System.out.println(
                "stack trace depth="
                        + Thread.currentThread().getStackTrace().length
        );

        System.out.println();
        System.out.println(
                "localValue=" + localValue
                        + ", heapObjectType=" + heapObject.getClass().getName()
        );
    }

    private static void printUsage(MemoryUsage usage) {
        System.out.println("used=" + toMiB(usage.getUsed()) + " MiB");
        System.out.println("committed=" + toMiB(usage.getCommitted()) + " MiB");
        System.out.println(
                "max="
                        + (usage.getMax() < 0
                        ? "undefined"
                        : toMiB(usage.getMax()) + " MiB")
        );
    }

    private static long toMiB(long bytes) {
        return bytes / 1024 / 1024;
    }
}
