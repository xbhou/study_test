package dev.xbhou.javalab.spring.lifecycle;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

public class BeanFactoryVsApplicationContextDemo {

    public static void main(String[] args) {
        bareBeanFactory();
        applicationContext();
    }

    private static void bareBeanFactory() {
        System.out.println("=== Bare BeanFactory ===");

        Probe.reset();

        DefaultListableBeanFactory beanFactory =
                new DefaultListableBeanFactory();

        beanFactory.registerBeanDefinition(
                "probe",
                new RootBeanDefinition(Probe.class)
        );

        System.out.println(
                "after registerBeanDefinition: constructors="
                        + Probe.constructorCount()
        );

        beanFactory.getBean("probe");

        System.out.println(
                "after getBean: constructors="
                        + Probe.constructorCount()
        );

        System.out.println();
    }

    private static void applicationContext() {
        System.out.println("=== ApplicationContext ===");

        Probe.reset();

        try (GenericApplicationContext context =
                     new GenericApplicationContext()) {

            context.registerBean("probe", Probe.class);

            System.out.println(
                    "before refresh: constructors="
                            + Probe.constructorCount()
            );

            context.refresh();

            System.out.println(
                    "after refresh: constructors="
                            + Probe.constructorCount()
            );
        }
    }

    public static class Probe {

        private static final AtomicInteger CONSTRUCTORS =
                new AtomicInteger();

        public Probe() {
            CONSTRUCTORS.incrementAndGet();
            System.out.println("Probe constructor");
        }

        static void reset() {
            CONSTRUCTORS.set(0);
        }

        static int constructorCount() {
            return CONSTRUCTORS.get();
        }
    }
}
