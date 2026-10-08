package dev.xbhou.javalab.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class MetaspaceOomDemo {

    public static void main(String[] args) {
        List<ClassLoader> retainedLoaders = new ArrayList<>();
        List<Class<?>> retainedClasses = new ArrayList<>();

        int generated = 0;

        while (true) {
            ClassLoader loader =
                    new ClassLoader(
                            MetaspaceOomDemo.class.getClassLoader()
                    ) {
                    };

            Object proxy = Proxy.newProxyInstance(
                    loader,
                    new Class<?>[]{Runnable.class},
                    (instance, method, methodArgs) -> null
            );

            retainedLoaders.add(loader);
            retainedClasses.add(proxy.getClass());

            generated++;

            if (generated % 500 == 0) {
                System.out.println(
                        "generated proxy classes=" + generated
                                + ", metaspaceUsed="
                                + metaspaceUsedMiB()
                                + " MiB"
                );
            }
        }
    }

    private static long metaspaceUsedMiB() {
        return ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getName().contains("Metaspace"))
                .findFirst()
                .map(MemoryPoolMXBean::getUsage)
                .map(usage -> usage.getUsed() / 1024 / 1024)
                .orElse(-1L);
    }
}
