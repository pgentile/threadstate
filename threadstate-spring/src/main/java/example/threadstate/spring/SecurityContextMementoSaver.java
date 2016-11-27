package example.threadstate.spring;

import example.threadstate.core.memento.ThreadStateMemento;
import example.threadstate.core.memento.ThreadStateMementoSaver;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextMementoSaver implements ThreadStateMementoSaver {

    @Override
    public ThreadStateMemento save() {
        final SecurityContext securityContext = SecurityContextHolder.getContext();

        return new ThreadStateMemento() {

            @Override
            public void restore() {
                SecurityContextHolder.setContext(securityContext);
            }

            @Override
            public void cleanup() {
                SecurityContextHolder.clearContext();
            }

        };
    }

}
