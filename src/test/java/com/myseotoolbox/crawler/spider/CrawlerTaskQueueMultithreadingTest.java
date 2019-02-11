package com.myseotoolbox.crawler.spider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CrawlerTaskQueueMultithreadingTest {


    CrawlerTaskQueue sut;
    @Mock private CrawlersPool pool;

    @Test(timeout = 500)
    public void shouldRequireProcessingOfAllSeeds() throws InterruptedException {

        CountDownLatch taskSubmittedLatch = buildLatchToSignalTaskSubmitted();

        sut = new CrawlerTaskQueue(uris("http://host1"), pool);
        Thread thread = new Thread(() -> sut.start());
        thread.start();


        taskSubmittedLatch.await();
        sut.onScanCompleted(uri("http://host1"), uris());

        thread.join();


        verify(pool).submit(uri("http://host1"));
    }

    @Test(timeout = 500)
    public void runBlocksUntilAllUriAreCrawled() throws InterruptedException {

        CountDownLatch taskSubmittedLatch = buildLatchToSignalTaskSubmitted();


        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"), pool);

        Thread thread = new Thread(() -> sut.start());
        thread.start();

        taskSubmittedLatch.await();

        sut.onScanCompleted(uri("http://host1"), uris());
        sut.onScanCompleted(uri("http://host2"), uris());

        thread.join();

        verify(pool).submit(uri("http://host1"));
        verify(pool).submit(uri("http://host2"));

    }

    @Test(timeout = 500)
    public void runAwaitsForCrawlsInProgressToBeFinishedAsTheyCouldDiscoverMoreLinks() throws InterruptedException {

        CountDownLatch taskSubmittedLatch = buildLatchToSignalTaskSubmitted();


        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"), pool);

        AtomicBoolean runFinished = new AtomicBoolean(false);


        Thread thread = new Thread(() -> {
            sut.start();
            runFinished.set(true);
        });

        thread.start();
        taskSubmittedLatch.await();

        while (thread.getState() != Thread.State.WAITING) {
            //Make sure the thread has a bit of time to reach the lock
            Thread.sleep(1);
        }

        assertFalse(runFinished.get());

        sut.onScanCompleted(uri("http://host1"), uris());
        sut.onScanCompleted(uri("http://host2"), uris());

        thread.join();

        //Assert that thread is released after notification
        assertTrue(runFinished.get());

    }

    @Test(timeout = 500)
    public void runBlockUntilDiscoveredUriAreCrawled() throws InterruptedException {

        CountDownLatch taskSubmittedLatch = buildLatchToSignalTaskSubmitted();


        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"), pool);

        AtomicBoolean runFinished = new AtomicBoolean(false);


        Thread thread = new Thread(() -> {
            sut.start();
            runFinished.set(true);
        });

        thread.start();
        taskSubmittedLatch.await();

        sut.onScanCompleted(uri("http://host1"), uris());
        sut.onScanCompleted(uri("http://host2"), uris("/dst"));

        while (thread.getState() != Thread.State.WAITING) {
            //Make sure the thread has a bit of time to reach the lock
            Thread.sleep(1);
        }

        assertFalse(runFinished.get());
        sut.onScanCompleted(uri("http://host2/dst"), uris());

        thread.join();

        //Assert that thread is released after notification
        assertTrue(runFinished.get());

    }

    private List<URI> uris(String... s) {
        return Stream.of(s).map(URI::create).collect(Collectors.toList());
    }


    private URI uri(String uri) {
        return URI.create(uri);
    }

    private CountDownLatch buildLatchToSignalTaskSubmitted() {
        CountDownLatch tasksSubmitted = new CountDownLatch(1);

        doAnswer(invocation -> {
            tasksSubmitted.countDown();
            return null;
        }).when(pool).submit(any());

        return tasksSubmitted;
    }
}
