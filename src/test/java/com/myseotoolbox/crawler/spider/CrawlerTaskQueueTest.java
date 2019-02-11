package com.myseotoolbox.crawler.spider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class CrawlerTaskQueueTest {

    public static final String LOCATION_WITH_UNICODE_CHARACTERS = "/família";


    CrawlerTaskQueue sut;
    @Mock private CrawlersPool pool;

    @Before
    public void setUp() throws Exception {
        doAnswer(invocation -> {
            URI newLink = invocation.getArgument(0);
            sut.onScanCompleted(newLink, uris());
            return null;
        }).when(pool).submit(any());
    }

    @Test(timeout = 500)
    public void shouldNotVisitTheSameUrlTwice() {

        whenCrawling("http://host1").discover("http://host1");

        sut = new CrawlerTaskQueue(uris("http://host1"), pool);
        sut.start();

        verify(pool).submit(uri("http://host1"));
    }

    @Test(timeout = 500)
    public void shouldNotVisitTwiceIfRelativeWasVisitedAndAbsoluteIsDiscovered() {

        whenCrawling("http://host1").discover("/dst");
        whenCrawling("http://host1/dst").discover("/dst");

        sut = new CrawlerTaskQueue(uris("http://host1"), pool);

        sut.start();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host1/dst"));

    }

    @Test(timeout = 500)
    public void shouldVisitTwiceIfDifferentVersionOfRootIsDiscovered() {

        whenCrawling("http://host1").discover("/");

        sut = new CrawlerTaskQueue(uris("http://host1"), pool);

        sut.start();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host1/"));
    }


    @Test(timeout = 500)
    public void takeAlwaysReturnAbsoluteUri() {
        whenCrawling("http://host1").discover("/dst");

        sut = new CrawlerTaskQueue(uris("http://host1"), pool);

        sut.start();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host1/dst"));
    }


    @Test(timeout = 500)
    public void onSnapshotCompleteShouldNeverBeFedRelativeUrisAsTakeNeverReturnsIt() {

        sut = new CrawlerTaskQueue(uris("http://host1/dst"), pool);

        try {
            sut.onScanCompleted(URI.create("/dst"), uris());
        } catch (IllegalStateException e) {
            //success!!
            return;
        }

        fail("Expected exception");
    }

    @Test(timeout = 500)
    public void completingASnapshotNeverSubmittedShouldThrowException() {

        sut = new CrawlerTaskQueue(uris("http://host1/dst"), pool);

        try {
            sut.onScanCompleted(URI.create("http://host1/dst"), uris());
        } catch (IllegalStateException e) {
            //success!!
            return;
        }

        fail("Expected exception");
    }

    @Test(timeout = 500)
    public void shouldAlertIfCompleteTheSameSnapshotTwice() {


        sut = new CrawlerTaskQueue(uris("http://host1"), pool);
        sut.start();

        try {
            sut.onScanCompleted(uri("http://host1"), uris());
        } catch (IllegalStateException e) {
            //success!!
            return;
        }

        fail("Expected exception");
    }

    @Test(timeout = 500)
    public void shouldProcessAllSeedsWithMultipleSeeds() throws InterruptedException {

        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"), pool);
        sut.start();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host2"));

    }


    @Test(timeout = 500)
    public void canResolveLinksWithUnicodeChars() {

        whenCrawling("http://host1").discover(LOCATION_WITH_UNICODE_CHARACTERS);


        sut = new CrawlerTaskQueue(uris("http://host1"), pool);

        sut.start();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host1" + "/fam%EDlia"));


    }

    @Test(timeout = 500)
    public void discoveringDuplicateLinksInPageDoesNotEnqueueItMultipleTimes() {
        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2", "http://host1/dst1");

        sut = new CrawlerTaskQueue(uris("http://host1"), pool);

        sut.start();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host1/dst1"));
        verify(pool).submit(uri("http://host1/dst2"));
    }

    @Test(timeout = 500)
    public void submittingDuplicatedUrlWhileItIsInProgressShouldNotQueueItTwice() throws InterruptedException {

        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2");


        CountDownLatch dst2ScanCompleted = new CountDownLatch(1);

        doAnswer(invocation -> {
            new Thread(() -> {
                URI newLink = invocation.getArgument(0);
                System.out.println("I'll wait here...." + newLink);
                try {
                    dst2ScanCompleted.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Done waiting...");
                sut.onScanCompleted(newLink, uris());
            }).start();
            return null;
        }).when(pool).submit(uri("http://host1/dst1"));


        doAnswer(invocation -> {
            new Thread(() -> {
                URI newLink = invocation.getArgument(0);
                System.out.println("Submitting duplicate:" + newLink);
                sut.onScanCompleted(newLink, uris("http://host1/dst1"));
                dst2ScanCompleted.countDown();
            }).start();
            return null;
        }).when(pool).submit(uri("http://host1/dst2"));


        sut = new CrawlerTaskQueue(uris("http://host1"), pool);
        sut.start();


        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host1/dst1"));
        verify(pool).submit(uri("http://host1/dst2"));
    }


    private List<URI> uris(String... s) {
        return Stream.of(s).map(URI::create).collect(Collectors.toList());
    }

    private CrawlerTaskQueueTestMockBuilder whenCrawling(String baseUri) {
        return new CrawlerTaskQueueTestMockBuilder(baseUri);
    }


    private URI uri(String uri) {
        return URI.create(uri);
    }

    private class CrawlerTaskQueueTestMockBuilder {
        private final String baseUri;

        public CrawlerTaskQueueTestMockBuilder(String baseUri) {
            this.baseUri = baseUri;
        }

        public void discover(String... discoveredUris) {
            doAnswer(invocation -> {
                URI newLink = invocation.getArgument(0);
                sut.onScanCompleted(newLink, uris(discoveredUris));
                return null;
            }).when(pool).submit(uri(baseUri));
        }
    }
}