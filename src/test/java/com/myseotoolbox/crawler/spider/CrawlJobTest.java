package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static java.net.URI.create;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CrawlJobTest {


    public static final UriFilter NO_URI_FILTER = (s, d) -> true;
    public static final int SINGLE_THREAD = 1;
    public static final URI TEST_ORIGIN = URI.create("http://domain1");
    public static final int MAX_CRAWLS = 1000;
    @Mock private WebPageReader pageReader;
    @Mock private Consumer<CrawlResult> subscriber;
    @Mock private Consumer<CrawlResult> exceptionSubscriber;


    @Before
    public void setUp() throws Exception {
        when(pageReader.snapshotPage(any())).thenAnswer(arguments -> CrawlResult.forSnapshot(PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(arguments.getArguments()[0].toString())));
        doThrow(new RuntimeException("This should be fine...")).when(exceptionSubscriber).accept(any());
    }

    @Test
    public void shouldFilterOutSeedsFromOutsideOrigin() {
        CrawlJob sut = new CrawlJob(TEST_ORIGIN, Arrays.asList(create("http://domain1"), create("http://domain2")), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService(), MAX_CRAWLS);
        sut.subscribeToPageCrawled(subscriber);
        sut.start();
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void shouldNotifySubscribers() {
        CrawlJob sut = new CrawlJob(TEST_ORIGIN, Collections.singletonList(create("http://domain1")), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService(), MAX_CRAWLS);
        sut.subscribeToPageCrawled(subscriber);
        sut.start();
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void shouldOnlyVisitSeedsNotTheRoot() {
        CrawlJob sut = new CrawlJob(TEST_ORIGIN, Arrays.asList(create("http://domain1/path1"), create("http://domain1/path2")), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService(), MAX_CRAWLS);
        sut.subscribeToPageCrawled(subscriber);
        sut.start();
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1/path1")));
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1/path2")));
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void listenerThrowingExceptionsShouldNotPreventOthersToGetMessage() {
        CrawlJob sut = new CrawlJob(TEST_ORIGIN, Collections.singletonList(create("http://domain1")), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService(), MAX_CRAWLS);
        sut.subscribeToPageCrawled(exceptionSubscriber);
        sut.subscribeToPageCrawled(subscriber);
        sut.start();
        Mockito.verify(exceptionSubscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void shouldNotVisitDuplicateSeeds() throws SnapshotException {
        CrawlJob sut = new CrawlJob(TEST_ORIGIN, Arrays.asList(create("http://domain1"), create("http://domain1")), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService(), MAX_CRAWLS);
        sut.start();
        verify(pageReader).snapshotPage(create("http://domain1"));
        verifyNoMoreInteractions(pageReader);
    }
}