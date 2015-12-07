package example.threadstate.examples;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import example.threadstate.core.memento.MementoTaskWrapper;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import example.threadstate.slf4j.MDCMementoSaver;
import example.threadstate.spring.DelegatedExecutorBeanPostProcessor;
import example.threadstate.spring.RequestContextMementoSaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
@EnableAutoConfiguration
public class ExampleApplication {

    public static void main(String... args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public ThreadStateMementoSaver threadStateMementoSaver() {
        final MDCMementoSaver mdcMementoSaver = new MDCMementoSaver();
        final RequestContextMementoSaver requestContextMementoSaver = new RequestContextMementoSaver();
        return mdcMementoSaver.andThen(requestContextMementoSaver);
    }

    @Bean
    public ExecutorService defaultExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("main-executor-%d")
            .build();
        return Executors.newCachedThreadPool(threadFactory);
    }

    @Bean
    public DelegatedExecutorBeanPostProcessor mementoExecutorBeanPostProcessor() {
        final MementoTaskWrapper taskWrapper = new MementoTaskWrapper(threadStateMementoSaver());
        return new DelegatedExecutorBeanPostProcessor(taskWrapper);
    }

}
