package example.threadstate.core.memento;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class CompositeThreadStateMementoSaverTest {

    @Mock
    private ThreadStateMementoSaver saver1;

    @Mock
    private ThreadStateMementoSaver saver2;

    @Mock
    private ThreadStateMemento memento1;

    @Mock
    private ThreadStateMemento memento2;

    @Test
    public void should_save_items_on_composite_save() throws Exception {
        // given
        given(saver1.save()).willReturn(memento1);
        given(saver2.save()).willReturn(memento2);

        // when
        final ThreadStateMementoSaver compositeSaver = new CompositeThreadStateMementoSaver(Arrays.asList(saver1, saver2));
        final ThreadStateMemento memento = compositeSaver.save();

        // then
        assertThat(compositeSaver).isExactlyInstanceOf(CompositeThreadStateMementoSaver.class);
        assertThat(memento).isExactlyInstanceOf(CompositeThreadStateMemento.class);

        InOrder inOrder = inOrder(saver1, saver2);
        inOrder.verify(saver1).save();
        inOrder.verify(saver2).save();
        inOrder.verifyNoMoreInteractions();
    }

}
