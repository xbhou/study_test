package dev.xbhou.javalab.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class LifecycleBean
        implements BeanNameAware,
        BeanFactoryAware,
        InitializingBean,
        DisposableBean {

    private final LifecycleEventRecorder recorder;
    private Dependency dependency;

    public LifecycleBean(LifecycleEventRecorder recorder) {
        this.recorder = recorder;
        recorder.record("constructor");
    }

    @Autowired
    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
        recorder.record(
                "dependency injection: setDependency("
                        + dependency.name()
                        + ")"
        );
    }

    @Override
    public void setBeanName(String name) {
        recorder.record(
                "BeanNameAware.setBeanName(" + name + ")"
        );
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory)
            throws BeansException {
        recorder.record("BeanFactoryAware.setBeanFactory");
    }

    @PostConstruct
    public void postConstruct() {
        recorder.record("@PostConstruct");
    }

    @Override
    public void afterPropertiesSet() {
        recorder.record(
                "InitializingBean.afterPropertiesSet"
        );
    }

    public void customInit() {
        recorder.record("custom initMethod");
    }

    public void businessMethod() {
        if (dependency == null) {
            throw new IllegalStateException(
                    "dependency should have been injected"
            );
        }

        recorder.record("business method");
    }

    @PreDestroy
    public void preDestroy() {
        recorder.record("@PreDestroy");
    }

    @Override
    public void destroy() {
        recorder.record("DisposableBean.destroy");
    }

    public void customDestroy() {
        recorder.record("custom destroyMethod");
    }
}
