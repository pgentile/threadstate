package example.threadstate.spring;

import example.threadstate.core.executors.DelegatedExecutor;
import example.threadstate.core.executors.DelegatedExecutorService;
import example.threadstate.core.executors.DelegatedScheduledExecutorService;
import example.threadstate.core.executors.TaskWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DelegatedExecutorBeanPostProcessorTest {

    @Mock
    private TaskWrapper taskWrapper;

    private AnnotationConfigApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        applicationContext = new AnnotationConfigApplicationContext();

        ReflectionUtils.doWithFields(
            getClass(),
            field -> {
                ReflectionUtils.makeAccessible(field);
                final Object obj = ReflectionUtils.getField(field, this);
                applicationContext.getBeanFactory().registerSingleton(field.getName(), obj);
            },
            field -> field.isAnnotationPresent(Mock.class)
        );

        applicationContext.register(SpringConfig.class);
        applicationContext.refresh();
    }

    @Test
    public void should_wrap_executors() throws Exception {
        // given
        final Runnable runnable = mock(Runnable.class);

        // when
        final Executor executor = applicationContext.getBean("executor", Executor.class);
        final ExecutorService executorService = applicationContext.getBean("executorService", ExecutorService.class);
        final ScheduledExecutorService scheduledExecutorService = applicationContext.getBean("scheduledExecutorService", ScheduledExecutorService.class);

        executor.execute(runnable);
        executorService.execute(runnable);
        scheduledExecutorService.execute(runnable);

        // then
        assertThat(executor).isExactlyInstanceOf(DelegatedExecutor.class);
        assertThat(executorService).isExactlyInstanceOf(DelegatedExecutorService.class);
        assertThat(scheduledExecutorService).isExactlyInstanceOf(DelegatedScheduledExecutorService.class);

        verify(taskWrapper, times(3)).wrap(any());
    }

    @After
    public void tearDown() throws Exception {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @Configuration
    public static class SpringConfig {

        @Autowired
        private TaskWrapper taskWrapper;

        @Bean
        public DelegatedExecutorBeanPostProcessor wrappedTaskExecutorBeanPostProcessor() {
            return new DelegatedExecutorBeanPostProcessor(taskWrapper);
        }

        @Bean
        public Executor executor() {
            return mock(Executor.class);
        }

        @Bean
        public ExecutorService executorService() {
            return mock(ExecutorService.class);
        }

        @Bean
        public ScheduledExecutorService scheduledExecutorService() {
            return mock(ScheduledExecutorService.class);
        }

    }

}
