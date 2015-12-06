package example.threadstate.core.memento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

final class CompositeThreadStateMemento implements ThreadStateMemento {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeThreadStateMemento.class);

    private final List<ThreadStateMemento> mementos;

    private boolean cleaned;

    public CompositeThreadStateMemento(List<ThreadStateMemento> mementos) {
        this.mementos = mementos;
    }

    @Override
    public void restore() {
        boolean ok = false;
        int count = 0;
        try {
            for (final ThreadStateMemento memento : mementos) {
                memento.restore();
                count++;
            }
            ok = true;
        } finally {
            if (!ok) {
                cleanupWithCount(count);
            }
        }
    }

    @Override
    public void cleanup() {
        cleanupWithCount(mementos.size());
    }

    private void cleanupWithCount(int count) {
        if (!cleaned) {
            cleaned = true;
            for (int i = count - 1; i >= 0; i--) {
                cleanupSilently(mementos.get(i));
            }
        }
    }

    private static void cleanupSilently(ThreadStateMemento memento) {
        try {
            memento.cleanup();
        } catch (final RuntimeException e) {
            LOGGER.error("Caught exception while cleaning memento {}", memento, e);
        }
    }

}
