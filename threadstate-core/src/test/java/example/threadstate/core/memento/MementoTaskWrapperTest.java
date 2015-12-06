package example.threadstate.core.memento;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MementoTaskWrapperTest {

    @InjectMocks
    private MementoTaskWrapper mementoTaskWrapper;

    @Mock
    private ThreadStateMementoSaver saver;

    @Mock
    private Callable<Void> callable;

    @Mock
    private ThreadStateMemento memento;

    @Test
    public void should_save_restore_and_cleanup_on_call_task_success() throws Exception {
        // given
        given(saver.save()).willReturn(memento);

        // when
        final Callable<Void> wrapped = mementoTaskWrapper.wrap(callable);
        final Object result = wrapped.call();

        // then
        assertThat(result).isNull();

        final InOrder inOrder = inOrder(saver, memento, callable);
        inOrder.verify(saver).save();
        inOrder.verify(memento).restore();
        inOrder.verify(callable).call();
        inOrder.verify(memento).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_save_restore_and_cleanup_on_call_task_failure() throws Exception {
        // given
        given(saver.save()).willReturn(memento);

        final Exception exception = new Exception();
        given(callable.call()).willThrow(exception);

        // when
        final Callable<Void> wrapped = mementoTaskWrapper.wrap(callable);
        final Throwable resultException = catchThrowable(wrapped::call);

        // then
        assertThat(resultException).isSameAs(exception);

        final InOrder inOrder = inOrder(saver, memento, callable);
        inOrder.verify(saver).save();
        inOrder.verify(memento).restore();
        inOrder.verify(callable).call();
        inOrder.verify(memento).cleanup();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_not_cleanup_on_call_task_with_restore_failure() throws Exception {
        // given
        given(saver.save()).willReturn(memento);

        final RuntimeException exception = new RuntimeException();
        doThrow(exception).when(memento).restore();

        // when
        final Callable<Void> wrapped = mementoTaskWrapper.wrap(callable);
        final Throwable resultException = catchThrowable(wrapped::call);

        // then
        assertThat(resultException).isSameAs(exception);

        final InOrder inOrder = inOrder(saver, memento);
        inOrder.verify(saver).save();
        inOrder.verify(memento).restore();
        inOrder.verifyNoMoreInteractions();

        verify(callable, never()).call();
        verify(memento, never()).cleanup();
    }

}
