package example.threadstate.slf4j;

import example.threadstate.core.memento.ThreadStateMemento;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import org.slf4j.MDC;

import java.util.Map;

public class MDCMementoSaver implements ThreadStateMementoSaver {

    @Override
    public ThreadStateMemento save() {
        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return new ThreadStateMemento() {

            @Override
            public void restore() {
                MDC.setContextMap(mdcContext);
            }

            @Override
            public void cleanup() {
                MDC.clear();
            }

        };
    }

}
