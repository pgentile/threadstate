package example.threadstate.core.memento;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MDCMementoSaverTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Map<String, String> savedMdc;

    @Before
    public void setUp() throws Exception {
        savedMdc = MDC.getCopyOfContextMap();
    }

    @After
    public void tearDown() throws Exception {
        MDC.setContextMap(savedMdc);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    @Test
    public void should_restore_non_empty_mdc_with_success() throws Exception {
        // given
        fillMDC();

        final Map<String, String> currentMdc = MDC.getCopyOfContextMap();

        final MDCMementoSaver mdcMementoSaver = new MDCMementoSaver();
        final ThreadStateMemento memento = mdcMementoSaver.save();

        // when

        final Map<String, String> threadMdc = executor.submit(() -> {
            memento.restore();
            return MDC.getCopyOfContextMap();
        }).get();

        // then
        assertThat(threadMdc).isEqualTo(currentMdc);
    }

    @Test
    public void should_restore_empty_mdc_with_success() throws Exception {
        // given
        MDC.clear();

        final MDCMementoSaver mdcMementoSaver = new MDCMementoSaver();
        final ThreadStateMemento memento = mdcMementoSaver.save();

        // when

        final Map<String, String> threadMdc = executor.submit(() -> {
            memento.restore();
            return MDC.getCopyOfContextMap();
        }).get();

        // then
        assertThat(threadMdc).isNullOrEmpty();
    }

    @Test
    public void should_cleanup_mdc_with_success() throws Exception {
        // given
        fillMDC();

        final MDCMementoSaver mdcMementoSaver = new MDCMementoSaver();
        final ThreadStateMemento memento = mdcMementoSaver.save();

        // when
        final Map<String, String> threadMdc = executor.submit(() -> {
            memento.restore();
            memento.cleanup();
            return MDC.getCopyOfContextMap();
        }).get();

        // then
        assertThat(threadMdc).isNullOrEmpty();
    }

    private static void fillMDC() {
        MDC.put("a", "1");
        MDC.put("b", "2");
    }

}
