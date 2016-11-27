package example.threadstate.core.concurrent;

import javaslang.control.Try;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static example.threadstate.core.concurrent.CompletableFutures.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public class CompletableFuturesTest {

    @Test
    public void should_return_empty_list_on_merge_empty() throws Exception {
        // given
        final List<CompletableFuture<Object>> futures = Collections.emptyList();

        // when
        final CompletableFuture<List<Object>> merged = merge(futures);

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
        final CompletableFuture<List<Object>> merged = merge(futures);

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
        final CompletableFuture<List<Object>> merged = merge(futures);

        // then
        assertThat(merged).isCompletedWithValue(items);
    }

    @Test
    public void should_not_be_completed_on_merge_uncompleted_items() throws Exception {
        // given
        final List<CompletableFuture<Object>> futures = Arrays.asList(
            CompletableFuture.completedFuture(new Object()),
            new CompletableFuture<>()
        );

        // when
        final CompletableFuture<List<Object>> merged = merge(futures);

        // then
        assertThat(merged).isNotDone();
    }

    @Test
    public void should_return_map_on_merge_map_with_multiple_items() throws Exception {
        // given
        final Map<String, CompletableFuture<Integer>> futures = new HashMap<>();
        futures.put("A", CompletableFuture.completedFuture(1));
        futures.put("B", CompletableFuture.completedFuture(2));
        futures.put("C", CompletableFuture.completedFuture(3));

        // when
        final CompletableFuture<Map<String, Integer>> merged = mergeMap(futures);

        // then

        final Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("A", 1);
        expectedResult.put("B", 2);
        expectedResult.put("C", 3);

        assertThat(merged).isCompletedWithValue(expectedResult);
    }

    @Test
    public void should_return_empty_map_on_merge_empty_map() throws Exception {
        // given
        final Map<String, CompletableFuture<Integer>> futures = new HashMap<>();

        // when
        final CompletableFuture<Map<String, Integer>> merged = mergeMap(futures);

        // then
        assertThat(merged).isCompletedWithValue(Collections.emptyMap());
    }

    @Test(timeout = 1000L)
    public void should_await() throws Exception {
        // given
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        final long waitTimeMs = 150;

        // when
        final long startTimestampMs = System.currentTimeMillis();
        final CompletableFuture<?> future = await(scheduledExecutor, waitTimeMs, TimeUnit.MILLISECONDS);
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
        final CompletableFuture<?> future = completedExceptionally(exception).thenApply(s -> "VALUE");

        // then
        assertThat(future).isCompletedExceptionally();
    }

    @Test
    public void should_unwrap_exception_with_completion_exception_with_cause() throws Exception {
        // given
        final Exception causeException = new Exception();
        final CompletionException completionException = new CompletionException(causeException);

        // when
        final Throwable resultException = unwrapException(completionException);

        // then
        assertThat(resultException).isSameAs(causeException);
    }

    @Test
    public void should_unwrap_exception_with_completion_exception_without_cause() throws Exception {
        // given
        final Exception causeException = null;
        final CompletionException completionException = new CompletionException(causeException);

        // when
        final Throwable resultException = unwrapException(completionException);

        // then
        assertThat(resultException).isSameAs(completionException);
    }

    @Test
    public void should_unwrap_exception_with_any_exception_different_of_completion_exception() throws Exception {
        // given
        final Exception exception = new Exception();

        // when
        final Throwable resultException = unwrapException(exception);

        // then
        assertThat(resultException).isSameAs(exception);
    }

    @Test
    public void should_not_rewrap_a_completion_exception() throws Exception {
        // given
        final Exception exception = new CompletionException(new RuntimeException());

        // when
        final CompletionException resultException = wrapException(exception);

        // then
        assertThat(resultException).isSameAs(exception);
    }

    @Test
    public void should_rewrap_exception() throws Exception {
        // given
        final Exception exception = new RuntimeException();

        // when
        final CompletionException resultException = wrapException(exception);

        // then
        assertThat(resultException).isInstanceOf(CompletionException.class).hasCause(exception);
    }

    @Test
    public void should_transform_failed_future_to_empty_optional() throws Exception {
        // given
        final CompletableFuture<String> future = completedExceptionally(new RuntimeException()).thenApply(s -> "VALUE");

        // when
        final CompletableFuture<Optional<String>> resultFuture = future.handle(toOptional());

        // then
        assertThat(resultFuture).isCompletedWithValue(Optional.empty());
    }

    @Test
    public void should_transform_successful_future_to_valued_optional() throws Exception {
        // given
        final String value = "OK";
        final CompletableFuture<String> future = CompletableFuture.completedFuture(value);

        // when
        final CompletableFuture<Optional<String>> resultFuture = future.handle(toOptional());

        // then
        assertThat(resultFuture).isCompletedWithValue(Optional.of(value));
    }

    @Test
    public void should_unwrap_exception_in_handle_method() throws Exception {
        // given
        final RuntimeException inputException = new RuntimeException();
        final CompletableFuture<String> future = completedExceptionally(inputException).thenApply(s -> "VALUE");

        // when
        final CompletableFuture<Boolean> resultFuture = future.handle(withUnwrappedException((value, exception) -> {
            return exception == inputException;
        }));

        // then
        assertThat(resultFuture).isCompletedWithValue(true);
    }

    @Test
    public void should_unwrap_exception_in_exceptionally_method() throws Exception {
        // given
        final RuntimeException inputException = new RuntimeException();
        final CompletableFuture<Boolean> future = completedExceptionally(inputException).thenApply(s -> false);

        // when
        final CompletableFuture<Boolean> resultFuture = future.exceptionally(withUnwrappedException(exception -> {
            return exception == inputException;
        }));

        // then
        assertThat(resultFuture).isCompletedWithValue(true);
    }

    @Test
    public void should_transform_successful_future_to_success_try() throws Exception {
        // given
        final String value = "OK";
        final CompletableFuture<String> future = CompletableFuture.completedFuture(value);

        // when
        final CompletableFuture<Try<String>> resultFuture = future.handle(toTry());

        // then
        assertThat(resultFuture).isCompletedWithValue(Try.success(value));
    }

    @Test
    public void should_transform_failed_future_to_failed_try() throws Exception {
        // given
        final RuntimeException inputException = new RuntimeException();
        final CompletableFuture<String> future = completedExceptionally(inputException).thenApply(s -> "VALUE");

        // when
        final CompletableFuture<Try<String>> resultFuture = future.handle(toTry());

        // then
        assertThat(resultFuture).isCompletedWithValue(Try.failure(inputException));
    }

}
