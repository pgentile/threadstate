package example.threadstate.core.executors;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface TaskWrapper {

    <T> Callable<T> wrap(Callable<T> callable);

    default TaskWrapper andThen(final TaskWrapper other) {
        final TaskWrapper that = this;
        return new TaskWrapper() {

            @Override
            public <T> Callable<T> wrap(final Callable<T> callable) {
                return that.wrap(other.wrap(callable));
            }

        };
    }

}
