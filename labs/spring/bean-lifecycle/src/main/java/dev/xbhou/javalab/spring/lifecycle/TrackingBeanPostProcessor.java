package dev.xbhou.javalab.spring.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

public class TrackingBeanPostProcessor
        implements BeanPostProcessor, PriorityOrdered {

    private final LifecycleEventRecorder recorder;

    public TrackingBeanPostProcessor(
            LifecycleEventRecorder recorder
    ) {
        this.recorder = recorder;
    }

    @Override
    public Object postProcessBeforeInitialization(
            Object bean,
            String beanName
    ) throws BeansException {
        if (bean instanceof LifecycleBean) {
            recorder.record(
                    "BeanPostProcessor.beforeInitialization"
            );
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(
            Object bean,
            String beanName
    ) throws BeansException {
        if (bean instanceof LifecycleBean) {
            recorder.record(
                    "BeanPostProcessor.afterInitialization"
            );
        }

        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
