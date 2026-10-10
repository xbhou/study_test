package dev.xbhou.javalab.spring.lifecycle;

import java.util.List;

public final class LifecycleOrderVerifier {

    private static final List<String> EXPECTED = List.of(
            "constructor",
            "dependency injection: setDependency(demo-dependency)",
            "BeanNameAware.setBeanName(lifecycleBean)",
            "BeanFactoryAware.setBeanFactory",
            "BeanPostProcessor.beforeInitialization",
            "@PostConstruct",
            "InitializingBean.afterPropertiesSet",
            "custom initMethod",
            "BeanPostProcessor.afterInitialization",
            "business method",
            "@PreDestroy",
            "DisposableBean.destroy",
            "custom destroyMethod"
    );

    private LifecycleOrderVerifier() {
    }

    public static void verify(List<String> actual) {
        if (!actual.equals(EXPECTED)) {
            throw new IllegalStateException(
                    "unexpected lifecycle order%nexpected=%s%nactual=%s"
                            .formatted(EXPECTED, actual)
            );
        }

        System.out.println();
        System.out.println(
                "Lifecycle order verified successfully."
        );
    }
}
