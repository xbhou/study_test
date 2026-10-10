package dev.xbhou.javalab.spring.lifecycle;

import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {

    public static void main(String[] args) {
        LifecycleEventRecorder recorder = AppConfig.recorder();
        recorder.reset();

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(
                        AppConfig.class
                );

        LifecycleBean bean =
                context.getBean(LifecycleBean.class);

        bean.businessMethod();

        context.close();

        List<String> events = recorder.snapshot();
        LifecycleOrderVerifier.verify(events);
    }
}
