package example.threadstate.core.executors;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface TaskWrapper {

    <T> Callable<T> wrap(Callable<T> callable);

}
