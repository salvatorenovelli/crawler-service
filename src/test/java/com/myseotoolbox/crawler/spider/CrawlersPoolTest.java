package com.myseotoolbox.crawler.spider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.SnapshotResult;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlersPoolTest {

    public static final PageSnapshot FAILURE_TEST_SNAPSHOT = new PageSnapshot();

    private static final SnapshotResult TEST_SNAPSHOT_RESULT = SnapshotResult.forSnapshot(new PageSnapshot());
    private static final URI SUCCESS_TEST_LINK = URI.create("http://host1");
    private static final URI FAILURE_TEST_LINK = URI.create("http://verybadhost");
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private WebPageReader reader;
    @Mock private Consumer<SnapshotResult> listener;
    @Mock private ExecutorService executor;
    private CrawlersPool sut;

    @Before
    public void setUp() throws SnapshotException {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        when(reader.snapshotPage(SUCCESS_TEST_LINK)).thenReturn(TEST_SNAPSHOT_RESULT);
        when(reader.snapshotPage(FAILURE_TEST_LINK)).thenThrow(new SnapshotException(new RuntimeException("This one's not good"), FAILURE_TEST_SNAPSHOT));

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CrawlersPool.class.getName());
        logger.addAppender(mockAppender);

        sut = new CrawlersPool(reader, executor);
    }

    @Test
    public void shouldSubmitSnapshotWhenSuccessful() {
        sut.accept(new SnapshotTask(SUCCESS_TEST_LINK, listener));
        verify(listener).accept(TEST_SNAPSHOT_RESULT);
    }

    @Test
    public void shouldSubmitPartialValueWhenExceptionOccur() {
        sut.accept(new SnapshotTask(FAILURE_TEST_LINK, listener));
        verify(listener).accept(argThat(argument -> argument.getPageSnapshot() == FAILURE_TEST_SNAPSHOT));
    }

    @Test
    public void exceptionHappeningOutsideCrawlShouldBeHandled() {

        doThrow(new RuntimeException("This happened while submitting result")).when(listener).accept(any());

        sut.accept(new SnapshotTask(SUCCESS_TEST_LINK, listener));

        verify(mockAppender).doAppend(argThat(argument -> {
            assertThat(argument.getLevel(), is(Level.ERROR));
            assertThat(argument.getMessage(), containsString("Exception while crawling"));
            return true;
        }));

    }
}
