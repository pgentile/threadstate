package example.threadstate.core.retry;

import example.threadstate.core.concurrent.CompletableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AsyncRetryExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRetryExecutor.class);

    private final ScheduledExecutorService scheduledExecutor;

    private final Executor primaryExecutor;

    private final Executor retryExecutor;

    private final RetryPolicy retryPolicy;

    private Predicate<Exception> abortRetryPredicate = ignored -> false;

    public AsyncRetryExecutor(
        final ScheduledExecutorService scheduledExecutor,
        final Executor primaryExecutor,
        final Executor retryExecutor,
        final RetryPolicy retryPolicy
    ) {
        this.scheduledExecutor = scheduledExecutor;
        this.primaryExecutor = primaryExecutor;
        this.retryExecutor = retryExecutor;
        this.retryPolicy = retryPolicy;
    }

    public AsyncRetryExecutor(
        final ScheduledExecutorService scheduledExecutor,
        final Executor executor,
        final RetryPolicy retryPolicy
    ) {
        this(scheduledExecutor, executor, executor, retryPolicy);
    }

    public void setAbortRetryPredicate(Predicate<Exception> abortRetryPredicate) {
        this.abortRetryPredicate = abortRetryPredicate;
    }

    public <T> CompletableFuture<T> execute(final Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, primaryExecutor)
            .thenApply(CompletableFuture::completedFuture)
            .exceptionally(e -> handleException(supplier, 1, e))
            .thenCompose(Function.identity());
    }

    public CompletableFuture<?> submit(Runnable runnable) {
        return execute(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> CompletableFuture<T> handleException(final Supplier<T> supplier, final int retryCount, final Throwable exception) {
        final Throwable realException = CompletableFutures.unwrapException(exception);

        if (realException instanceof Error || abortRetryPredicate.test((Exception) realException)) {
            LOGGER.debug("Failed to execute task, will not retry", realException);
            return CompletableFutures.completedExceptionally(new AbortRetryException(realException));
        }

        final OptionalLong waitDurationMs = retryPolicy.getWaitTime(retryCount);
        if (!waitDurationMs.isPresent()) {
            LOGGER.debug("Failed to execute task, got to max retries count of {}", retryCount - 1, realException);
            return CompletableFutures.completedExceptionally(new MaxRetryException(retryCount, realException));
        }

        LOGGER.debug("Failed to execute task, retrying after {} ms", waitDurationMs.getAsLong(), realException);
        return CompletableFutures.await(scheduledExecutor, waitDurationMs.getAsLong(), TimeUnit.MILLISECONDS)
            .thenApplyAsync(ignored -> supplier.get(), retryExecutor)
            .thenApply(CompletableFuture::completedFuture)
            .exceptionally(e -> handleException(supplier, retryCount + 1, e))
            .thenCompose(Function.identity());
    }

}
