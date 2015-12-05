package example.threadstate.core.memento;

import java.util.List;

class CompositeThreadStateMemento implements ThreadStateMemento {

    private final List<ThreadStateMemento> mementos;

    public CompositeThreadStateMemento(List<ThreadStateMemento> mementos) {
        this.mementos = mementos;
    }

    @Override
    public void restore() {
        mementos.forEach(ThreadStateMemento::restore);
    }

    @Override
    public void cleanup() {
        mementos.forEach(ThreadStateMemento::cleanup);
    }

}
