package example.threadstate.core.executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class TaskWrapperTest {

    @Mock
    private Watcher watcher1;

    @Mock
    private Watcher watcher2;

    @Mock
    private Callable<?> callable;

    @Test
    public void should_chain_wrappers() throws Exception {
        // given
        final TaskWrapper wrapper1 = new TaskWrapper() {

            @Override
            public <T> Callable<T> wrap(Callable<T> callable) {
                return () -> {
                    watcher1.begin();
                    try {
                        return callable.call();
                    } finally {
                        watcher1.end();
                    }
                };
            }

        };

        final TaskWrapper wrapper2 = new TaskWrapper() {

            @Override
            public <T> Callable<T> wrap(Callable<T> callable) {
                return () -> {
                    watcher2.begin();
                    try {
                        return callable.call();
                    } finally {
                        watcher2.end();
                    }
                };
            }

        };

        final Object obj = new Object();
        given(callable.call()).willReturn(obj);

        // when
        final TaskWrapper chainedWrapper = wrapper1.andThen(wrapper2);
        final Callable<?> outputCallable = chainedWrapper.wrap(callable);
        final Object callResult = outputCallable.call();

        // then
        assertThat(outputCallable).isNotNull();
        assertThat(callResult).isSameAs(obj);

        final InOrder inOrder = inOrder(watcher1, watcher2, callable);
        inOrder.verify(watcher1).begin();
        inOrder.verify(watcher2).begin();
        inOrder.verify(callable).call();
        inOrder.verify(watcher2).end();
        inOrder.verify(watcher1).end();
        inOrder.verifyNoMoreInteractions();
    }

    public interface Watcher {

        void begin();

        void end();

    }

}
