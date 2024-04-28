package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.event.CrawlCompletedEvent;
import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.spider.event.CrawlStatusUpdateEvent;
import com.myseotoolbox.crawler.spider.event.MessageBrokerEventDispatch;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;

import java.util.Collections;

import static com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory.newWebsiteCrawlFor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MessageBrokerEventDispatchTest {


    private static final String PAGE_CRAWL_COMPLETED_TOPIC = "pageCrawlCompletedTopic";
    private static final String WEBSITE_CRAWL_COMPLETED_TOPIC = "websiteCrawlCompletedTopic";
    private static final String CRAWL_STATUS_UPDATE_TOPIC = "crawl-status-update";
    @Mock private PubSubPublisherTemplate template;
    private PubSubProperties config = new PubSubProperties(WEBSITE_CRAWL_COMPLETED_TOPIC, PAGE_CRAWL_COMPLETED_TOPIC, CRAWL_STATUS_UPDATE_TOPIC, 10);
    MessageBrokerEventDispatch sut;

    @Before
    public void setUp() throws Exception {
        sut = new MessageBrokerEventDispatch(template, config);
    }

    @Test
    public void shouldPublishWebsiteCompletedOnTheCorrectQueue() {

        PageSnapshot val = new PageSnapshot();
        val.setUri("http://host/someuri");
        sut.pageCrawlCompletedEvent("123", val);
        verify(template).publish(eq(PAGE_CRAWL_COMPLETED_TOPIC), eq(new PageCrawlCompletedEvent("123", val)));
    }

    @Test
    public void shouldPublishPageCrawlCompletedOnTheCorrectQueue() {
        WebsiteCrawl websiteCrawl = newWebsiteCrawlFor("host.host", Collections.emptyList());
        sut.onCrawlCompletedEvent(new CrawlCompletedEvent(websiteCrawl));
        verify(template).publish(eq(WEBSITE_CRAWL_COMPLETED_TOPIC), eq(new CrawlCompletedEvent(websiteCrawl)));
    }

    @Test
    public void shouldPublishStatusUpdateOnMessageBroker() {
        WebsiteCrawl websiteCrawl = newWebsiteCrawlFor("host.host", Collections.emptyList());
        CrawlStatusUpdateEvent event = new CrawlStatusUpdateEvent(10, 100, websiteCrawl);
        sut.onCrawlStatusUpdate(event);
        verify(template).publish(CRAWL_STATUS_UPDATE_TOPIC, event);
    }
}