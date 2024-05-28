package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.spider.event.*;
import com.myseotoolbox.crawler.spider.ratelimiter.TestClockUtils;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MessageBrokerEventListenerTest {
    private static WebsiteCrawl TEST_CRAWL = TestWebsiteCrawlFactory.newWebsiteCrawlFor("http://host/", Collections.emptyList());
    private static WebsiteCrawl TEST_CRAWL2 = TestWebsiteCrawlFactory.newWebsiteCrawlFor("http://host/", Collections.emptyList());

    private static final String PAGE_CRAWL_COMPLETED_TOPIC = "pageCrawlCompletedTopic";
    private static final String WEBSITE_CRAWL_STARTED_TOPIC = "websiteCrawlStartedTopic";
    private static final String WEBSITE_CRAWL_COMPLETED_TOPIC = "websiteCrawlCompletedTopic";
    private static final String CRAWL_STATUS_UPDATE_TOPIC = "crawl-status-update";
    private static final int TEST_INTERVAL_MS = 500;

    @Mock private PubSubPublisherTemplate template;
    private final ClockUtils testClockUtils = new TestClockUtils();

    private PubSubProperties config = new PubSubProperties(
            WEBSITE_CRAWL_STARTED_TOPIC,
            WEBSITE_CRAWL_COMPLETED_TOPIC,
            PAGE_CRAWL_COMPLETED_TOPIC,
            new PubSubProperties.TopicConfiguration(CRAWL_STATUS_UPDATE_TOPIC, TEST_INTERVAL_MS), 10);

    MessageBrokerEventListener sut;

    @Before
    public void setUp() throws Exception {
        sut = new MessageBrokerEventListener(template, config, new RateLimiterRepository(config.getCrawlStatusUpdateConfiguration().getTopicPublishMinIntervalMillis(), testClockUtils, 100));
    }

    @Test
    public void shouldPublishWebsiteCompletedOnTheCorrectQueue() {
        PageSnapshot val = PageSnapshotTestBuilder.aTestPageSnapshotForUri("http://host/someuri").build();
        sut.onPageCrawlCompletedEvent(new PageCrawledEvent(TEST_CRAWL, CrawlResult.forSnapshot(val)));
        verify(template).publish(eq(PAGE_CRAWL_COMPLETED_TOPIC), eq(new PageCrawlCompletedEvent(TEST_CRAWL.getId().toHexString(), val)));
    }

    @Test
    public void shouldPublishPageCrawlCompletedOnTheCorrectQueue() {
        WebsiteCrawlCompletedEvent event = new WebsiteCrawlCompletedEvent(TEST_CRAWL, Instant.EPOCH);
        sut.onWebsiteCrawlCompletedEvent(event);
        verify(template).publish(eq(WEBSITE_CRAWL_COMPLETED_TOPIC), eq(event));
    }

    @Test
    public void shouldPublishStatusUpdateOnMessageBroker() {
        CrawlStatusUpdateEvent event = new CrawlStatusUpdateEvent(TEST_CRAWL, 10, 100, Instant.EPOCH);
        sut.onCrawlStatusUpdate(event);
        verify(template).publish(CRAWL_STATUS_UPDATE_TOPIC, event);
    }


    @Test
    public void shouldNotPublishStatusUpdateMoreFrequentlyThanConfigured() throws InterruptedException {
        CrawlStatusUpdateEvent event = new CrawlStatusUpdateEvent(TEST_CRAWL, 10, 100, Instant.EPOCH);
        sut.onCrawlStatusUpdate(event); //run 1
        sut.onCrawlStatusUpdate(event); //no op (too early)
        sut.onCrawlStatusUpdate(event); //no op (too early)
        testClockUtils.sleep(TEST_INTERVAL_MS);
        sut.onCrawlStatusUpdate(event); //run 2
        verify(template, times(2)).publish(CRAWL_STATUS_UPDATE_TOPIC, event);
    }

    @Test
    public void multipleCrawlsShouldNotBeLimitedTogether() throws InterruptedException {
        CrawlStatusUpdateEvent event1 = new CrawlStatusUpdateEvent(TEST_CRAWL, 10, 100, Instant.EPOCH);
        CrawlStatusUpdateEvent event2 = new CrawlStatusUpdateEvent(TEST_CRAWL2, 10, 100, Instant.EPOCH);

        sut.onCrawlStatusUpdate(event1); //run 1
        sut.onCrawlStatusUpdate(event1); //no op (too early)
        sut.onCrawlStatusUpdate(event2); //run 2

        verify(template, times(1)).publish(CRAWL_STATUS_UPDATE_TOPIC, event1);
        verify(template, times(1)).publish(CRAWL_STATUS_UPDATE_TOPIC, event2);
    }

    @Test
    public void shouldPublishCrawlStartedEvent() {
        WebsiteCrawlStartedEvent event = WebsiteCrawlStartedEvent.from(TEST_CRAWL, Instant.EPOCH);
        sut.onWebsiteCrawlStarted(event);
        verify(template).publish(WEBSITE_CRAWL_STARTED_TOPIC, event);
    }
}