package example.threadstate.spring;

import example.threadstate.core.executors.DelegatedExecutor;
import example.threadstate.core.executors.DelegatedExecutorService;
import example.threadstate.core.executors.DelegatedScheduledExecutorService;
import example.threadstate.core.executors.TaskWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class DelegatedExecutorBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedExecutorBeanPostProcessor.class);

    private TaskWrapper taskWrapper;

    public DelegatedExecutorBeanPostProcessor() {
    }

    public DelegatedExecutorBeanPostProcessor(TaskWrapper taskWrapper) {
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

        // Already wrapped or not processable
        if (bean instanceof DelegatedExecutor || !isProcessable(bean, beanName)) {
            LOGGER.info("Executor '{}' will not be wrapped", beanName);
            return bean;
        }

        LOGGER.info("Wrapping executor '{}'", beanName);

        if (bean instanceof ScheduledExecutorService) {
            return new DelegatedScheduledExecutorService((ScheduledExecutorService) bean, taskWrapper);
        }
        if (bean instanceof ExecutorService) {
            return new DelegatedExecutorService((ExecutorService) bean, taskWrapper);
        }
        return new DelegatedExecutor((Executor) bean, taskWrapper);
    }

    protected boolean isProcessable(Object bean, String beanName) {
        return true;
    }

}
