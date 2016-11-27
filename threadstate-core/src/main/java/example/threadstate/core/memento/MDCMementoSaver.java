package example.threadstate.core.memento;

import org.slf4j.MDC;

import java.util.Map;

public class MDCMementoSaver implements ThreadStateMementoSaver {

    @Override
    public ThreadStateMemento save() {
        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        if (mdcContext == null) {
            return new ThreadStateMemento() {

                @Override
                public void restore() {
                    MDC.clear();
                }

                @Override
                public void cleanup() {
                    MDC.clear();
                }

            };
        }

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
