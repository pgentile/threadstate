package example.threadstate.core.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CompletableFutures {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletableFutures.class);

    private CompletableFutures() {
        // Utility class
    }

    public static Throwable unwrapException(final Throwable exception) {
        if (exception instanceof CompletionException && exception.getCause() != null) {
            LOGGER.trace("Unwrapping from CompletionException", exception);
            return exception.getCause();
        }
        return exception;
    }

    public static <T> CompletableFuture<T> completedExceptionally(final Throwable exception) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        return future;
    }

    public static <T> CompletableFuture<List<T>> merge(final List<CompletableFuture<T>> futures) {
        final int size = futures.size();

        if (size == 0) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        final Iterator<CompletableFuture<T>> iterator = futures.iterator();

        CompletableFuture<List<T>> mergedFuture = iterator.next().thenApply(item -> {
            final List<T> items = new ArrayList<>(size);
            items.add(item);
            return items;
        });

        while (iterator.hasNext()) {
            mergedFuture = mergedFuture.thenCombine(iterator.next(), (items, item) -> {
                items.add(item);
                return items;
            });
        }

        return mergedFuture.thenApply(Collections::unmodifiableList);
    }

    public static <T> CompletableFuture<T> await(
        final ScheduledExecutorService scheduledExecutor,
        final long delay,
        final TimeUnit timeUnit
    ) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        scheduledExecutor.schedule(() -> future.complete(null), delay, timeUnit);
        return future;
    }

}
