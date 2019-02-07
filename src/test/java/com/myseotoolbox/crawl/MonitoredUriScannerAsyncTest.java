package com.myseotoolbox.crawl;


import com.myseotoolbox.crawl.config.AsyncMonitoredUriScanConfig;
import com.myseotoolbox.crawl.httpclient.WebPageScraper;
import com.myseotoolbox.crawl.model.Workspace;
import com.myseotoolbox.crawl.repository.MonitoredUriRepository;
import com.myseotoolbox.crawl.repository.WorkspaceRepository;
import com.myseotoolbox.crawl.testutils.WorkspaceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.myseotoolbox.crawl.config.AsyncMonitoredUriScanConfig.MAX_CONCURRENT_SCAN_COUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MonitoredUriScannerAsyncTest {

    @Mock private WebPageScraper scraper;
    @Mock private MonitoredUriRepository monitoredUriRepo;
    @Mock private WorkspaceRepository workspaceRepo;

    MonitoredUriScanner sut;


    @Before
    public void setUp() {
        AsyncMonitoredUriScanConfig config = new AsyncMonitoredUriScanConfig();
        sut = new MonitoredUriScanner(scraper, monitoredUriRepo, workspaceRepo, config.threadPoolTaskExecutor());
    }

    @Test(timeout = 2000)
    public void scanAllshouldScanMultipleWorkspaceConcurrently() throws InterruptedException {

        //Given a number of workspaces equal to the number of maximum concurrent workspace scan

        Stream<Workspace> workspaceStream = IntStream.range(0, MAX_CONCURRENT_SCAN_COUNT).mapToObj(WorkspaceBuilder::createWorkspace);
        when(workspaceRepo._getAll()).thenReturn(workspaceStream);


        //And that the scan gets blocked until all the scan has started
        //This will cause all the threads to wait until all the executor threads have reached this point.
        CountDownLatch countDownLatch = new CountDownLatch(MAX_CONCURRENT_SCAN_COUNT);
        when(monitoredUriRepo.countByWorkspaceNumber(any(Integer.class))).then((Answer<Long>) invocation -> {
            countDownLatch.countDown();
            countDownLatch.await();
            return 0L;
        });


        sut.scanAll();

        //This lock will never be released unless all the scan are started concurrently causing the test to timeout and fail
        countDownLatch.await();

    }

}

