package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.myseotoolbox.crawler.spider.ExecutorBuilder.buildExecutor;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CrawlJobTest {


    public static final Predicate<URI> NO_URI_FILTER = uri -> true;
    public static final int SINGLE_THREAD = 1;
    @Mock private WebPageReader pageReader;
    @Mock private Consumer<PageSnapshot> subscriber;
    @Mock private Consumer<PageSnapshot> exceptionSubscriber;


    @Before
    public void setUp() throws Exception {
        when(pageReader.snapshotPage(any())).thenAnswer(arguments -> PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(arguments.getArguments()[0].toString()));
        doThrow(new RuntimeException("This should be fine...")).when(exceptionSubscriber).accept(any());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfSeedsOriginDontMatchWebsiteOrigin() {
        CrawlJob sut = new CrawlJob(create("http://domain1"), asList(create("http://domain1"), create("http://domain2")), pageReader, NO_URI_FILTER, buildExecutor(SINGLE_THREAD));
    }

    @Test
    public void shouldNotifySubscribers() {
        CrawlJob sut = new CrawlJob(create("http://domain1"), Collections.emptyList(), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService());
        sut.subscribeToCrawlCompleted(subscriber);
        sut.start();
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void listenerThrowingExceptionsShouldNotPreventOthersToGetMessage() {
        CrawlJob sut = new CrawlJob(create("http://domain1"), Collections.emptyList(), pageReader, NO_URI_FILTER, new CurrentThreadTestExecutorService());
        sut.subscribeToCrawlCompleted(exceptionSubscriber);
        sut.subscribeToCrawlCompleted(subscriber);
        sut.start();
        Mockito.verify(exceptionSubscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));
        Mockito.verify(subscriber).accept(argThat(argument -> argument.getUri().equals("http://domain1")));

    }

    @Slf4j
    private static class CurrentThreadTestExecutorService extends AbstractExecutorService {

        @Override
        public void execute(Runnable command) {
            try {
                command.run();
            } catch (Exception e) {
                //Swallow leaked exception for consistency with executor service
                log.error("Exception in run: ", e);
            }
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException("Not implemented!");
        }

        @Override
        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException("Not implemented!");
        }

        @Override
        public boolean isShutdown() {
            throw new UnsupportedOperationException("Not implemented!");
        }

        @Override
        public boolean isTerminated() {
            throw new UnsupportedOperationException("Not implemented!");
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("Not implemented!");
        }
    }
}