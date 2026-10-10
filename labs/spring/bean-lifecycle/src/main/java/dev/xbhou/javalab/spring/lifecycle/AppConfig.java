package dev.xbhou.javalab.spring.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private static final LifecycleEventRecorder RECORDER =
            new LifecycleEventRecorder();

    static LifecycleEventRecorder recorder() {
        return RECORDER;
    }

    @Bean
    public static TrackingBeanPostProcessor trackingBeanPostProcessor() {
        return new TrackingBeanPostProcessor(RECORDER);
    }

    @Bean
    public Dependency dependency() {
        return new Dependency();
    }

    @Bean(
            initMethod = "customInit",
            destroyMethod = "customDestroy"
    )
    public LifecycleBean lifecycleBean() {
        return new LifecycleBean(RECORDER);
    }
}
