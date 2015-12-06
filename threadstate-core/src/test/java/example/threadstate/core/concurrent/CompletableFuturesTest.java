package example.threadstate.core.concurrent;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    @Test
    public void should_await() throws Exception {
        // given
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        // when
        final CompletableFuture<?> future = CompletableFutures.await(1, TimeUnit.SECONDS, scheduledExecutor);

        // then
        assertThat(future).isNotDone();
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

}
