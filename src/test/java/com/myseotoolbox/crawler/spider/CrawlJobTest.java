package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.SnapshotResult;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.function.Consumer;

import static java.net.URI.create;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CrawlJobTest {


    public static final UriFilter NO_URI_FILTER = (s, d) -> true;
    public static final int SINGLE_THREAD = 1;
    @Mock private WebPageReader pageReader;
    @Mock private Consumer<PageSnapshot> subscriber;
    @Mock private Consumer<PageSnapshot> exceptionSubscriber;


    @Before
    public void setUp() throws Exception {
        when(pageReader.snapshotPage(any())).thenAnswer(arguments -> SnapshotResult.forSnapshot(PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(arguments.getArguments()[0].toString())));
        doThrow(new RuntimeException("This should be fine...")).when(exceptionSubscriber).accept(any());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfSeedsOriginDontMatchWebsiteOrigin() {
        new CrawlJob(create("http://domain1"), asList(create("http://domain1"), create("http://domain2")), pageReader, NO_URI_FILTER, new ExecutorBuilder().buildExecutor("", SINGLE_THREAD));
    }

    @Test
    public void shouldNotifySubscribers() {
        CrawlJob sut = new CrawlJob(create("http://domain1"), Collections.emptyList(), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService());
        sut.subscribeToPageCrawled(subscriber);
        sut.start();
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void listenerThrowingExceptionsShouldNotPreventOthersToGetMessage() {
        CrawlJob sut = new CrawlJob(create("http://domain1"), Collections.emptyList(), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService());
        sut.subscribeToPageCrawled(exceptionSubscriber);
        sut.subscribeToPageCrawled(subscriber);
        sut.start();
        Mockito.verify(exceptionSubscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));

    }
}