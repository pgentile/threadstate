package example.threadstate.spring;

import example.threadstate.core.memento.ThreadStateMemento;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestContextMementoSaver implements ThreadStateMementoSaver {

    @Override
    public ThreadStateMemento save() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return new ThreadStateMemento() {

                @Override
                public void restore() {
                    RequestContextHolder.resetRequestAttributes();
                }

                @Override
                public void cleanup() {
                    RequestContextHolder.resetRequestAttributes();
                }

            };
        }

        return new ThreadStateMemento() {

            @Override
            public void restore() {
                RequestContextHolder.setRequestAttributes(attributes);
            }

            @Override
            public void cleanup() {
                RequestContextHolder.resetRequestAttributes();
            }

        };
    }

}
