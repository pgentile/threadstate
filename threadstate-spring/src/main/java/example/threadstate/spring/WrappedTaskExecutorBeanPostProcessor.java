package example.threadstate.spring;

import example.threadstate.core.executors.DelegatedExecutor;
import example.threadstate.core.executors.DelegatedExecutorService;
import example.threadstate.core.executors.DelegatedScheduledExecutorService;
import example.threadstate.core.executors.TaskWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class WrappedTaskExecutorBeanPostProcessor implements BeanPostProcessor {

    private TaskWrapper taskWrapper;

    public WrappedTaskExecutorBeanPostProcessor() {
    }

    public WrappedTaskExecutorBeanPostProcessor(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
    }

    public void setTaskWrapper(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof Executor)) {
            return bean;
        }

        // No task wrapper defined
        if (taskWrapper == null) {
            return bean;
        }

        // Already wrapped
        if (bean instanceof DelegatedExecutor) {
            return bean;
        }

        if (bean instanceof ScheduledExecutorService) {
            return new DelegatedScheduledExecutorService((ScheduledExecutorService) bean, taskWrapper);
        }
        if (bean instanceof ExecutorService) {
            return new DelegatedExecutorService((ExecutorService) bean, taskWrapper);
        }
        return new DelegatedExecutor((Executor) bean, taskWrapper);
    }

}
