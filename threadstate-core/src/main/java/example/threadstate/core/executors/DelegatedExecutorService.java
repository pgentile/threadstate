package example.threadstate.core.executors;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DelegatedExecutorService extends DelegatedExecutor implements ExecutorService {

    private final ExecutorService delegate;

    private final TaskWrapper taskWrapper;

    public DelegatedExecutorService(ExecutorService delegate, TaskWrapper taskWrapper) {
        super(delegate, taskWrapper);
        this.delegate = delegate;
        this.taskWrapper = taskWrapper;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }


    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }


    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrap(task));
    }


    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(wrap(task), result);
    }


    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrap(task));
    }


    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(wrapMany(tasks));
    }


    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(wrapMany(tasks), timeout, unit);
    }


    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(wrapMany(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(wrapMany(tasks), timeout, unit);
    }

    protected final <T> Callable<T> wrap(Callable<T> callable) {
        return taskWrapper.wrap(callable);
    }

    protected final <T> Collection<? extends Callable<T>> wrapMany(Collection<? extends Callable<T>> callables) {
        return callables.stream()
                .map(this::wrap)
                .collect(Collectors.toList());
    }

}
