package example.threadstate.core.concurrent;

import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static CompletionException wrapException(final Throwable exception) {
        if (exception instanceof CompletionException) {
            return (CompletionException) exception;
        }
        return new CompletionException(exception);
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

    public static <K, T> CompletableFuture<Map<K, T>> mergeMap(final Map<K, CompletableFuture<T>> futures, Supplier<Map<K, T>> mapFactory) {
        final int size = futures.size();

        if (size == 0) {
            return CompletableFuture.completedFuture(Collections.emptyMap());
        }

        final Iterator<Map.Entry<K, CompletableFuture<T>>> iterator = futures.entrySet().iterator();

        final Map.Entry<K, CompletableFuture<T>> firstEntry = iterator.next();
        CompletableFuture<Map<K, T>> mergedFuture = firstEntry.getValue().thenApply(item -> {
            final Map<K, T> items = mapFactory.get();
            items.put(firstEntry.getKey(), item);
            return items;
        });

        while (iterator.hasNext()) {
            final Map.Entry<K, CompletableFuture<T>> entry = iterator.next();
            mergedFuture = mergedFuture.thenCombine(entry.getValue(), (items, item) -> {
                items.put(entry.getKey(), item);
                return items;
            });
        }

        return mergedFuture.thenApply(Collections::unmodifiableMap);
    }

    public static <K, T> CompletableFuture<Map<K, T>> mergeMap(final Map<K, CompletableFuture<T>> futures) {
        return mergeMap(futures, HashMap::new);
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

    public static <T> BiFunction<T, Throwable, Optional<T>> toOptional() {
        return (value, exception) -> {
            if (exception != null) {
                LOGGER.debug("Ignoring exception", exception);
                return Optional.empty();
            }
            return Optional.of(value);
        };
    }

    public static <T> BiFunction<T, Throwable, Try<T>> toTry() {
        return (value, exception) -> {
            if (exception != null) {
                return Try.failure(unwrapException(exception));
            }
            return Try.success(value);
        };
    }

    public static <R> Function<Throwable, R> withUnwrappedException(Function<Throwable, ? extends R> function) {
        return exception -> {
            final Throwable unwrapped = unwrapException(exception);
            return function.apply(unwrapped);
        };
    }

    public static <T, R> BiFunction<T, Throwable, R> withUnwrappedException(BiFunction<? super T, Throwable, ? extends R> function) {
        return (value, exception) -> {
            final Throwable unwrapped = exception != null ? unwrapException(exception) : null;
            return function.apply(value, unwrapped);
        };
    }

}
