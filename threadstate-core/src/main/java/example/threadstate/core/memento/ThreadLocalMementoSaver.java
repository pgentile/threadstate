package example.threadstate.core.memento;

public class ThreadLocalMementoSaver<T> implements ThreadStateMementoSaver {

    private final ThreadLocal<T> local;

    public ThreadLocalMementoSaver(ThreadLocal<T> local) {
        this.local = local;
    }

    @Override
    public ThreadStateMemento save() {
        final T savedValue = local.get();
        return new ThreadStateMemento() {

            @Override
            public void restore() {
                local.set(savedValue);
            }

            @Override
            public void cleanup() {
                local.remove();
            }

        };
    }

}
