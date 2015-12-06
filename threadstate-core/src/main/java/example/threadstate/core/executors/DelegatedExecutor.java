package example.threadstate.core.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DelegatedExecutor implements Executor {

    private final Executor delegate;

    private final TaskWrapper taskWrapper;

    public DelegatedExecutor(Executor delegate, TaskWrapper taskWrapper) {
        this.delegate = delegate;
        this.taskWrapper = taskWrapper;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(wrap(command));
    }

    protected final Runnable wrap(Runnable runnable) {
        final Callable<Object> callable = taskWrapper.wrap(Executors.callable(runnable));
        return () -> {
            try {
                callable.call();
            } catch (final Exception e) {
                // TODO Replace with another exception?
                throw new RuntimeException(e);
            }
        };
    }

}
