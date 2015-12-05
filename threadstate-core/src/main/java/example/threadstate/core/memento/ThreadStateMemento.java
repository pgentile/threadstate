package example.threadstate.core.memento;

public interface ThreadStateMemento {

    void restore();

    void cleanup();

}
