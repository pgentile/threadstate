package example.threadstate.core.retry;

import example.threadstate.core.concurrent.CompletableFutures;

import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class AsyncRetryExecutor {

    private final ScheduledExecutorService scheduledExecutor;

    private final Executor primaryExecutor;

    private final Executor retryExecutor;

    private final RetryPolicy retryPolicy;

    public AsyncRetryExecutor(ScheduledExecutorService scheduledExecutor, Executor primaryExecutor, Executor retryExecutor, RetryPolicy retryPolicy) {
        this.scheduledExecutor = scheduledExecutor;
        this.primaryExecutor = primaryExecutor;
        this.retryExecutor = retryExecutor;
        this.retryPolicy = retryPolicy;
    }

    public AsyncRetryExecutor(ScheduledExecutorService scheduledExecutor, Executor executor, RetryPolicy retryPolicy) {
        this(scheduledExecutor, executor, executor, retryPolicy);
    }

    public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, primaryExecutor)
            .thenApply(CompletableFuture::completedFuture)
            .exceptionally(e -> handleException(supplier, 1, e))
            .thenCompose(Function.identity());
    }

    private <T> CompletableFuture<T> handleException(final Supplier<T> supplier, int count, Throwable exception) {
        final Throwable realException = CompletableFutures.unwrapException(exception);

        OptionalLong waitDurationMs = retryPolicy.getWaitTime(count);
        if (waitDurationMs.isPresent()) {
            return CompletableFutures.await(scheduledExecutor, waitDurationMs.getAsLong(), TimeUnit.MILLISECONDS)
                .thenApplyAsync(ignored -> supplier.get(), retryExecutor)
                .thenApply(CompletableFuture::completedFuture)
                .exceptionally(e -> handleException(supplier, count + 1, e))
                .thenCompose(Function.identity());
        }

        return CompletableFutures.completedExceptionally(new MaxRetryException(count, realException));
    }

}
