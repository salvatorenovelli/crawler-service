package com.myseotoolbox.crawler.spider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlersPoolTest {

    public static final PageSnapshot FAILURE_TEST_SNAPSHOT = new PageSnapshot();
    private static final CrawlResult TEST_SNAPSHOT_RESULT = CrawlResult.forSnapshot(new PageSnapshot());
    private static final URI SUCCESS_TEST_LINK = URI.create("http://host1");
    private static final URI FAILURE_TEST_LINK = URI.create("http://verybadhost");
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private WebPageReader reader;
    @Mock private Consumer<CrawlResult> listener;
    private ThreadPoolExecutor executor = new CurrentThreadTestExecutorService();
    private CrawlersPool sut;

    @Before
    public void setUp() throws SnapshotException {
        when(reader.snapshotPage(SUCCESS_TEST_LINK)).thenReturn(TEST_SNAPSHOT_RESULT);
        when(reader.snapshotPage(FAILURE_TEST_LINK)).thenThrow(new SnapshotException(new RuntimeException("This one's not good"), FAILURE_TEST_SNAPSHOT));

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CrawlersPool.class.getName());
        logger.addAppender(mockAppender);

        sut = new CrawlersPool(reader, executor);
    }

    @Test
    public void shouldSubmitSnapshotWhenSuccessful() {
        acceptTaskFor(SUCCESS_TEST_LINK);
        verify(listener).accept(TEST_SNAPSHOT_RESULT);
    }

    @Test
    public void shouldSubmitPartialValueWhenExceptionOccur() {
        acceptTaskFor(FAILURE_TEST_LINK);
        verify(listener).accept(argThat(argument -> argument.getPageSnapshot() == FAILURE_TEST_SNAPSHOT));
    }

    @Test
    public void shouldLogExceptionHappeningOutsideCrawl() {
        doThrow(new RuntimeException("This happened while submitting result")).when(listener).accept(any());
        acceptTaskFor(SUCCESS_TEST_LINK);
        verify(mockAppender).doAppend(argThat(argument -> argument.getLevel().equals(Level.ERROR) && argument.getMessage().contains("Exception while crawling")));
    }


    private void acceptTaskFor(URI uri) {
        sut.accept(new SnapshotTask(uri, listener));
    }
}
