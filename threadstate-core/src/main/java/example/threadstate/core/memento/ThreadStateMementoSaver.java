package example.threadstate.core.memento;

import java.util.Arrays;

@FunctionalInterface
public interface ThreadStateMementoSaver {

    ThreadStateMemento save();

    default ThreadStateMementoSaver andThen(ThreadStateMementoSaver other) {
        return new CompositeThreadStateMementoSaver(Arrays.asList(this, other));
    }

}
