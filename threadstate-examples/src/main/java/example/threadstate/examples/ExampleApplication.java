package example.threadstate.examples;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import example.threadstate.core.memento.MDCMementoSaver;
import example.threadstate.core.memento.MementoTaskWrapper;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import example.threadstate.spring.DelegatedExecutorBeanPostProcessor;
import example.threadstate.spring.RequestContextMementoSaver;
import example.threadstate.spring.SecurityContextMementoSaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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
    @Primary
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
    public DelegatedExecutorBeanPostProcessor requestMementoExecutorBeanPostProcessor() {
        final ThreadStateMementoSaver mementoSaver = new MDCMementoSaver()
            .andThen(new RequestContextMementoSaver())
            .andThen(new SecurityContextMementoSaver());

        final MementoTaskWrapper taskWrapper = new MementoTaskWrapper(mementoSaver);
        return new DelegatedExecutorBeanPostProcessor(taskWrapper);
    }

}
