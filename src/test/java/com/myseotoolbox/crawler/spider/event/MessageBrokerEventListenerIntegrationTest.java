package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory.newWebsiteCrawlFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("message-broker-test")
public class MessageBrokerEventListenerIntegrationTest {
    private final WebsiteCrawl TEST_WEBSITE_CRAWL = newWebsiteCrawlFor("host", Collections.emptyList());

    @Autowired PubSubProperties config;
    @MockBean PubSubPublisherTemplate pubSubTemplate;
    @Autowired MessageBrokerEventListener sut;
    @Autowired ApplicationEventPublisher publisher;

    @Test
    public void rateLimiterShouldBeConfiguredProperly() {
        assertThat(config.getCrawlStatusUpdateConfiguration().getTopicPublishMinIntervalMillis(), is(5000));

        publisher.publishEvent(new CrawlStatusUpdateEvent(1, 2, "ID1"));
        publisher.publishEvent(new CrawlStatusUpdateEvent(1, 2, "ID1"));//should ignore this as min delay is 5000

        verify(pubSubTemplate, times(1)).publish(eq(config.getCrawlStatusUpdateConfiguration().getTopicName()), any(CrawlStatusUpdateEvent.class));
    }

    @Test
    public void shouldForwardEvents() {
        WebsiteCrawl websiteCrawl = newWebsiteCrawlFor("host.host", Collections.emptyList());
        CrawlStartedEvent event = CrawlStartedEvent.from(websiteCrawl);
        publisher.publishEvent(event);
        verify(pubSubTemplate).publish(config.getWebsiteCrawlStartedTopicName(), event);
    }
}