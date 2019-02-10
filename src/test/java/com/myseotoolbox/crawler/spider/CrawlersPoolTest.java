package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrawlersPoolTest {

    private static final PageSnapshot TEST_SNAPSHOT = new PageSnapshot();
    private static final PageSnapshot FAILURE_TEST_SNAPSHOT = new PageSnapshot();
    private static final URI SUCCESS_TEST_LINK = URI.create("http://host1");
    private static final URI FAILURE_TEST_LINK = URI.create("http://verybadhost");
    @Mock private WebPageReader reader;
    @Mock private SnapshotCompletedListener listener;
    @Mock private ExecutorService executor;
    private CrawlersPool sut;

    @Before
    public void setUp() throws SnapshotException {
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        when(reader.snapshotPage(SUCCESS_TEST_LINK)).thenReturn(TEST_SNAPSHOT);
        when(reader.snapshotPage(FAILURE_TEST_LINK)).thenThrow(new SnapshotException(new RuntimeException("This one's not good"), FAILURE_TEST_SNAPSHOT));

        sut = new CrawlersPool(reader, listener, executor);
    }

    @Test
    public void shouldSubmitSnapshotWhenSuccessful() {
        sut.submit(SUCCESS_TEST_LINK);
        verify(listener).onSnapshotComplete(TEST_SNAPSHOT);
    }

    @Test
    public void shouldSubmitPartialValueWhenExceptionOccur() {
        sut.submit(FAILURE_TEST_LINK);
        verify(listener).onSnapshotComplete(argThat(argument -> argument == FAILURE_TEST_SNAPSHOT));
    }
}