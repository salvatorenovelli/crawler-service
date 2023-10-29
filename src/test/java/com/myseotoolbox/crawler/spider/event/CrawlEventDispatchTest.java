package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CrawlEventDispatchTest {
    private final static String TEST_URI = "http://example.com";

    private final WebsiteCrawl websiteCrawl = WebsiteCrawlFactory.newWebsiteCrawlFor(TEST_URI, Collections.singletonList(URI.create(TEST_URI)));
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    private CrawlEventDispatch sut;

    @Before
    public void setUp() throws Exception {
        sut = new CrawlEventDispatch(websiteCrawl, applicationEventPublisher);
    }

    @Test
    public void onPageCrawledShouldPublishPageCrawledEvent() {
        CrawlResult testCrawlResult = givenTestCrawlResultForUri(TEST_URI);
        sut.onPageCrawled(testCrawlResult);

        verify(applicationEventPublisher).publishEvent(argThat((PageCrawledEvent event) ->
                event.getCrawlResult().equals(testCrawlResult)
        ));
    }

    @Test
    public void onCrawlStartedShouldPublishCrawlStartedEvent() {
        sut.onCrawlStarted();

        verify(applicationEventPublisher).publishEvent(argThat((CrawlStartedEvent event) ->
                event.getWebsiteCrawl().equals(websiteCrawl)
        ));
    }

    @Test
    public void onCrawlCompletedShouldPublishCrawlCompletedEvent() {
        sut.onCrawlCompleted();

        verify(applicationEventPublisher).publishEvent(argThat((CrawlCompletedEvent event) ->
                event.getWebsiteCrawl().getOrigin().equals(websiteCrawl.getOrigin()) && event.getWebsiteCrawl().getId().equals(websiteCrawl.getId().toHexString())
        ));
    }

    private CrawlResult givenTestCrawlResultForUri(String uri) {
        return CrawlResult.forSnapshot(PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(uri));
    }

}
