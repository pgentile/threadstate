package example.threadstate.core.memento;

import example.threadstate.core.executors.TaskWrapper;

import java.util.concurrent.Callable;

public class MementoTaskWrapper implements TaskWrapper {

    private final ThreadStateMementoSaver saver;

    public MementoTaskWrapper(ThreadStateMementoSaver saver) {
        this.saver = saver;
    }

    @Override
    public <T> Callable<T> wrap(Callable<T> callable) {
        final ThreadStateMemento memento = saver.save();
        return () -> {
            memento.restore();
            try {
                return callable.call();
            } finally {
                memento.cleanup();
            }
        };
    }

}
