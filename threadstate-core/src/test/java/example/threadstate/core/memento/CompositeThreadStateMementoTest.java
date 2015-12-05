package example.threadstate.core.memento;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.inOrder;

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

        // when
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(Arrays.asList(memento1, memento2, memento3));
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

        // when
        final ThreadStateMemento compositeMemento = new CompositeThreadStateMemento(Arrays.asList(memento1, memento2, memento3));
        compositeMemento.cleanup();

        // then
        final InOrder inOrder = inOrder(memento1, memento2, memento3);
        inOrder.verify(memento3).cleanup();
        inOrder.verify(memento2).cleanup();
        inOrder.verify(memento1).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

}
