package com.myseotoolbox.crawler.spider.event;

import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("message-broker-test")
@Slf4j
public class MessageBrokerEventListenerIntegrationTest {
    private final WebsiteCrawl CRAWL = TestWebsiteCrawlFactory.newWebsiteCrawlFor("host", Collections.emptyList());

    @Autowired PubSubProperties config;
    @MockBean PubSubPublisherTemplate pubSubTemplate;
    @Autowired ApplicationEventPublisher publisher;
    @Autowired MessageBrokerEventListener sut;

    @Test
    public void rateLimiterShouldBeConfiguredProperly() {
        assertThat(config.getCrawlStatusUpdateConfiguration().getTopicPublishMinIntervalMillis(), is(5000));
        publisher.publishEvent(new CrawlStatusUpdateEvent(CRAWL, 1, 2, Instant.now()));
        publisher.publishEvent(new CrawlStatusUpdateEvent(CRAWL, 1, 2, Instant.now()));//should ignore this as min delay is 5000

        verify(pubSubTemplate, times(1)).publish(eq(config.getCrawlStatusUpdateConfiguration().getTopicName()), any(CrawlStatusUpdateEvent.class));
    }

    @Test
    public void shouldForwardEvents() {
        WebsiteCrawlStartedEvent event = WebsiteCrawlStartedEvent.from(CRAWL, Instant.EPOCH);
        publisher.publishEvent(event);
        verify(pubSubTemplate).publish(config.getWebsiteCrawlStartedTopicName(), event);
    }
}