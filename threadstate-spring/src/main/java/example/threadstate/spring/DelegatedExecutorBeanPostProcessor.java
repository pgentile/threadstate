package example.threadstate.spring;

import example.threadstate.core.executors.DelegatedExecutor;
import example.threadstate.core.executors.DelegatedExecutorService;
import example.threadstate.core.executors.DelegatedScheduledExecutorService;
import example.threadstate.core.executors.TaskWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiPredicate;

public class DelegatedExecutorBeanPostProcessor implements BeanPostProcessor, BeanNameAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedExecutorBeanPostProcessor.class);

    private String processorBeanName;

    private TaskWrapper taskWrapper;

    private BiPredicate<Object, String> beanSelector = (bean, beanName) -> true;

    public DelegatedExecutorBeanPostProcessor() {
    }

    public DelegatedExecutorBeanPostProcessor(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
    }

    @Override
    public void setBeanName(String name) {
        this.processorBeanName = name;
    }

    public void setTaskWrapper(TaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
    }

    public void setBeanSelector(BiPredicate<Object, String> beanSelector) {
        this.beanSelector = beanSelector;
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
        if (bean instanceof DelegatedExecutor || !beanSelector.test(bean, beanName)) {
            LOGGER.debug("Processor {}: Executor '{}' will not be wrapped", processorBeanName, beanName);
            return bean;
        }

        LOGGER.info("Processor {}: Wrapping executor '{}'", processorBeanName, beanName);

        if (bean instanceof ScheduledExecutorService) {
            return new DelegatedScheduledExecutorService((ScheduledExecutorService) bean, taskWrapper);
        }
        if (bean instanceof ExecutorService) {
            return new DelegatedExecutorService((ExecutorService) bean, taskWrapper);
        }
        return new DelegatedExecutor((Executor) bean, taskWrapper);
    }

}
