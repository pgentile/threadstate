package example.threadstate.core.retry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.OptionalLong;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AsyncRetryExecutorTest {

    private static final OptionalLong TEST_WAIT_DURATION_MS = OptionalLong.of(10);

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private final Executor primaryExecutor = Executors.newSingleThreadExecutor();

    private final Executor retryExecutor = Executors.newSingleThreadExecutor();

    @Mock
    private Supplier<Object> supplier;

    @Mock
    private RetryPolicy retryPolicy;

    @Test
    public void should_not_retry_on_execute_with_success() throws Exception {
        // given
        final AsyncRetryExecutor asyncRetryExecutor = new AsyncRetryExecutor(
            scheduledExecutor,
            primaryExecutor,
            retryExecutor,
            retryPolicy
        );

        given(retryPolicy.getWaitTime(anyInt())).willReturn(TEST_WAIT_DURATION_MS);

        final Object supplied = new Object();
        given(supplier.get()).willReturn(supplied);

        // when
        final Object result = asyncRetryExecutor.execute(supplier).get();

        // then
        assertThat(result).isSameAs(supplied);

        verify(supplier).get();
        verify(retryPolicy, never()).getWaitTime(anyInt());
    }

    @Test
    public void should_retry_and_succeed_on_execute_with_some_failures() throws Exception {
        // given
        final AsyncRetryExecutor asyncRetryExecutor = new AsyncRetryExecutor(
            scheduledExecutor,
            primaryExecutor,
            retryExecutor,
            retryPolicy
        );

        given(retryPolicy.getWaitTime(anyInt())).willReturn(TEST_WAIT_DURATION_MS);

        final RuntimeException exception = new RuntimeException();
        final Object supplied = new Object();
        given(supplier.get()).willThrow(exception, exception).willReturn(supplied);

        // when
        final Object result = asyncRetryExecutor.execute(supplier).get();

        // then
        assertThat(result).isSameAs(supplied);

        verify(supplier, times(3)).get();
        verify(retryPolicy, times(2)).getWaitTime(anyInt());
    }

    @Test
    public void should_retry_and_fail_on_recurrent_failure() throws Exception {
        // given
        final AsyncRetryExecutor asyncRetryExecutor = new AsyncRetryExecutor(
            scheduledExecutor,
            primaryExecutor,
            retryExecutor,
            retryPolicy
        );

        final OptionalLong waitDurationMs = TEST_WAIT_DURATION_MS;
        given(retryPolicy.getWaitTime(anyInt())).willReturn(waitDurationMs, waitDurationMs).willReturn(OptionalLong.empty());

        final RuntimeException exception = new RuntimeException();
        given(supplier.get()).willThrow(exception);

        // when
        final Throwable futureException = catchThrowable(() -> asyncRetryExecutor.execute(supplier).get());

        // then
        assertThat(futureException).isInstanceOf(ExecutionException.class).hasCauseInstanceOf(MaxRetryException.class);
        assertThat(futureException.getCause()).hasCause(exception);

        verify(supplier, times(3)).get();
        verify(retryPolicy, times(3)).getWaitTime(anyInt());
    }

}
