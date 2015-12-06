package example.threadstate.core.concurrent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public class CompletableFuturesTest {

    @Test
    public void should_return_empty_list_on_merge_empty() throws Exception {
        // given
        final List<CompletableFuture<Object>> futures = Collections.emptyList();

        // when
        final CompletableFuture<List<Object>> merged = CompletableFutures.merge(futures);

        // then
        assertThat(merged).isCompletedWithValueMatching(List::isEmpty, "empty list");
    }

    @Test
    public void should_return_one_item_on_merge_one_item() throws Exception {
        // given
        final List<Object> items = Collections.singletonList(new Object());

        final List<CompletableFuture<Object>> futures = items.stream()
            .map(CompletableFuture::completedFuture)
            .collect(Collectors.toList());

        // when
        final CompletableFuture<List<Object>> merged = CompletableFutures.merge(futures);

        // then
        assertThat(merged).isCompletedWithValue(items);
    }

    @Test
    public void should_return_many_items_on_merge_many_items() throws Exception {
        // given
        final List<Object> items = Arrays.asList(new Object(), new Object(), new Object());

        final List<CompletableFuture<Object>> futures = items.stream()
            .map(CompletableFuture::completedFuture)
            .collect(Collectors.toList());

        // when
        final CompletableFuture<List<Object>> merged = CompletableFutures.merge(futures);

        // then
        assertThat(merged).isCompletedWithValue(items);
    }

    @Test
    public void should_not_be_completed_on_merge_uncompleted_items() throws Exception {
        // given
        final List<CompletableFuture<Object>> futures = Arrays.asList(
            new CompletableFuture<>(),
            new CompletableFuture<>()
        );

        // when
        final CompletableFuture<List<Object>> merged = CompletableFutures.merge(futures);

        // then
        assertThat(merged).isNotDone();
    }

    @Test(timeout = 1000L)
    public void should_await() throws Exception {
        // given
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        final long waitTimeMs = 150;

        // when
        final long startTimestampMs = System.currentTimeMillis();
        final CompletableFuture<?> future = CompletableFutures.await(scheduledExecutor, waitTimeMs, TimeUnit.MILLISECONDS);
        final Object result = future.join();
        final long durationMs = System.currentTimeMillis() - startTimestampMs;

        // then
        assertThat(result).isNull();

        assertThat(durationMs).isCloseTo(waitTimeMs, withPercentage(20));
    }

    @Test
    public void should_complete_exceptionally() throws Exception {
        // given
        final Exception exception = new Exception();

        // when
        final CompletableFuture<?> future = CompletableFutures.completedExceptionally(exception);

        // then
        assertThat(future).isCompletedExceptionally();
    }

    @Test
    public void should_unwrap_exception_with_completion_exception_with_cause() throws Exception {
        // given
        final Exception causeException = new Exception();
        final CompletionException completionException = new CompletionException(causeException);

        // when
        final Throwable resultException = CompletableFutures.unwrapException(completionException);

        // then
        assertThat(resultException).isSameAs(causeException);
    }

    @Test
    public void should_unwrap_exception_with_completion_exception_without_cause() throws Exception {
        // given
        final Exception causeException = null;
        final CompletionException completionException = new CompletionException(causeException);

        // when
        final Throwable resultException = CompletableFutures.unwrapException(completionException);

        // then
        assertThat(resultException).isSameAs(completionException);
    }

    @Test
    public void should_unwrap_exception_with_any_exception_different_of_completion_exception() throws Exception {
        // given
        final Exception exception = new Exception();

        // when
        final Throwable resultException = CompletableFutures.unwrapException(exception);

        // then
        assertThat(resultException).isSameAs(exception);
    }

}
