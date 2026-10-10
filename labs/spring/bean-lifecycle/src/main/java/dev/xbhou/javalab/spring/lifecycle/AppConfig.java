package dev.xbhou.javalab.spring.lifecycle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public LifecycleEventRecorder lifecycleEventRecorder() {
        return new LifecycleEventRecorder();
    }

    @Bean
    public static TrackingBeanPostProcessor trackingBeanPostProcessor(
            LifecycleEventRecorder recorder
    ) {
        return new TrackingBeanPostProcessor(recorder);
    }

    @Bean
    public Dependency dependency() {
        return new Dependency();
    }

    @Bean(
            initMethod = "customInit",
            destroyMethod = "customDestroy"
    )
    public LifecycleBean lifecycleBean(
            LifecycleEventRecorder recorder
    ) {
        return new LifecycleBean(recorder);
    }
}
