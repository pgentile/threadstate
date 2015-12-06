package example.threadstate.core.memento;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class CompositeThreadStateMementoTest {

    @Mock
    private ThreadStateMemento memento1;

    @Mock
    private ThreadStateMemento memento2;

    @Mock
    private ThreadStateMemento memento3;

    @Test
    public void should_restore_items_on_restore() throws Exception {
        // given
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(asList(memento1, memento2, memento3));

        // when
        compositeMemento.restore();

        // then
        final InOrder inOrder = inOrder(memento1, memento2, memento3);
        inOrder.verify(memento1).restore();
        inOrder.verify(memento2).restore();
        inOrder.verify(memento3).restore();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_cleanup_items_on_cleanup() throws Exception {
        // given
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(asList(memento1, memento2, memento3));

        // when
        compositeMemento.cleanup();

        // then
        final InOrder inOrder = inOrder(memento1, memento2, memento3);
        inOrder.verify(memento3).cleanup();
        inOrder.verify(memento2).cleanup();
        inOrder.verify(memento1).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_cleanup_items_once() throws Exception {
        // given
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(asList(memento1, memento2, memento3));

        // when
        compositeMemento.cleanup();
        compositeMemento.cleanup();

        // then
        final InOrder inOrder = inOrder(memento1, memento2, memento3);
        inOrder.verify(memento3).cleanup();
        inOrder.verify(memento2).cleanup();
        inOrder.verify(memento1).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_ignore_exceptions_on_cleanup() throws Exception {
        // given
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(asList(memento1, memento2, memento3));

        doThrow(new RuntimeException()).when(memento2).cleanup();

        // when
        compositeMemento.cleanup();

        // then
        final InOrder inOrder = inOrder(memento1, memento2, memento3);
        inOrder.verify(memento3).cleanup();
        inOrder.verify(memento2).cleanup();
        inOrder.verify(memento1).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_cleanup_once_on_restore_failure() throws Exception {
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(asList(memento1, memento2, memento3));

        final RuntimeException exception = new RuntimeException();
        doThrow(exception).when(memento2).restore();

        // when
        final Throwable resultException = catchThrowable(compositeMemento::restore);
        compositeMemento.cleanup();

        // then
        assertThat(resultException).isSameAs(exception);

        final InOrder inOrder = inOrder(memento1, memento2, memento3);
        inOrder.verify(memento1).restore();
        inOrder.verify(memento2).restore();
        inOrder.verify(memento3, never()).restore();
        inOrder.verify(memento3, never()).cleanup();
        inOrder.verify(memento2, never()).cleanup();
        inOrder.verify(memento1).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

}
