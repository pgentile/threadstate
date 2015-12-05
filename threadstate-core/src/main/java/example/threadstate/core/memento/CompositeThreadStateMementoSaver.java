package example.threadstate.core.memento;

import java.util.ArrayList;
import java.util.List;

class CompositeThreadStateMementoSaver implements ThreadStateMementoSaver {

    private final List<ThreadStateMementoSaver> savers;

    public CompositeThreadStateMementoSaver(List<ThreadStateMementoSaver> savers) {
        this.savers = savers;
    }

    @Override
    public ThreadStateMemento save() {
        final List<ThreadStateMemento> mementos = new ArrayList<>(savers.size());
        for (ThreadStateMementoSaver saver : savers) {
            mementos.add(saver.save());
        }
        return new CompositeThreadStateMemento(mementos);
    }

    @Override
    public ThreadStateMementoSaver andThen(ThreadStateMementoSaver other) {
        final List<ThreadStateMementoSaver> newSavers = new ArrayList<>(savers);
        if (other instanceof CompositeThreadStateMementoSaver) {
            newSavers.addAll(((CompositeThreadStateMementoSaver) other).savers);
        } else {
            newSavers.add(other);
        }
        return new CompositeThreadStateMementoSaver(newSavers);
    }

}
