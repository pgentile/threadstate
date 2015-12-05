package example.threadstate.core.memento;

import java.util.List;

final class CompositeThreadStateMemento implements ThreadStateMemento {

    private final List<ThreadStateMemento> mementos;

    public CompositeThreadStateMemento(List<ThreadStateMemento> mementos) {
        this.mementos = mementos;
    }

    @Override
    public void restore() {
        for (ThreadStateMemento memento: mementos) {
            memento.restore();
        }
    }

    @Override
    public void cleanup() {
        for (int i = mementos.size() - 1; i >= 0; i--) {
            mementos.get(i).cleanup();
        }
    }

}
