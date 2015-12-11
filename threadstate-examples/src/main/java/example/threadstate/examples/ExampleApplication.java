package example.threadstate.examples;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import example.threadstate.core.memento.MementoTaskWrapper;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import example.threadstate.core.retry.AsyncRetryExecutor;
import example.threadstate.core.retry.RetryPolicy;
import example.threadstate.slf4j.MDCMementoSaver;
import example.threadstate.spring.DelegatedExecutorBeanPostProcessor;
import example.threadstate.spring.RequestContextMementoSaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
@EnableAutoConfiguration
public class ExampleApplication {

    public static void main(String... args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    public ExecutorService defaultExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("default-%d")
            .build();
        return Executors.newCachedThreadPool(threadFactory);
    }

    @Bean
    public ExecutorService asyncTaskExecutor() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("async-task-%d")
            .build();
        return Executors.newCachedThreadPool(threadFactory);
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("scheduled-%d")
            .build();
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    @Bean
    public AsyncRetryExecutor asyncRetryExecutor() {
        final RetryPolicy retryPolicy = RetryPolicy.fixedDuration(1000).withJitter(20).withMaxRetryCount(2);
        return new AsyncRetryExecutor(scheduledExecutorService(), asyncTaskExecutor(), retryPolicy);
    }

    @Bean
    public DelegatedExecutorBeanPostProcessor requestMementoExecutorBeanPostProcessor() {
        final ThreadStateMementoSaver mementoSaver = new MDCMementoSaver().andThen(new RequestContextMementoSaver());
        final MementoTaskWrapper taskWrapper = new MementoTaskWrapper(mementoSaver);
        final DelegatedExecutorBeanPostProcessor beanPostProcessor = new DelegatedExecutorBeanPostProcessor(taskWrapper);
        beanPostProcessor.setBeanSelector((bean, beanName) -> "defaultExecutor".equals(beanName));
        return beanPostProcessor;
    }

    @Bean
    public DelegatedExecutorBeanPostProcessor asyncMementoExecutorBeanPostProcessor() {
        final ThreadStateMementoSaver mementoSaver = new MDCMementoSaver();
        final MementoTaskWrapper taskWrapper = new MementoTaskWrapper(mementoSaver);
        final DelegatedExecutorBeanPostProcessor beanPostProcessor = new DelegatedExecutorBeanPostProcessor(taskWrapper);
        beanPostProcessor.setBeanSelector((bean, beanName) -> !"defaultExecutor".equals(beanName));
        return beanPostProcessor;
    }

}
