package example.threadstate.examples;

import example.threadstate.core.executors.TaskWrapper;
import example.threadstate.core.memento.MementoTaskWrapper;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import example.threadstate.slf4j.MDCMementoSaver;
import example.threadstate.spring.RequestContextMementoSaver;
import example.threadstate.spring.WrappedTaskExecutorBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ThreadStateMementoSaver threadStateMementoSaver() {
        final MDCMementoSaver mdcMementoSaver = new MDCMementoSaver();
        final RequestContextMementoSaver requestContextMementoSaver = new RequestContextMementoSaver();
        return mdcMementoSaver.andThen(requestContextMementoSaver);
    }

    @Bean
    public TaskWrapper taskWrapper() {
        return new MementoTaskWrapper(threadStateMementoSaver());
    }

    @Bean
    public ExecutorService executor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public WrappedTaskExecutorBeanPostProcessor wrappedTaskExecutorBeanPostProcessor() {
        return new WrappedTaskExecutorBeanPostProcessor(taskWrapper());
    }

}
