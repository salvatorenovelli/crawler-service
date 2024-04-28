package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBrokerEventDispatch {

    private final PubSubPublisherTemplate template;
    private final PubSubProperties config;

    public void onCrawlCompletedEvent(CrawlCompletedEvent event) {
        log.info("WebsiteCrawl completed. Publishing event. {}", event);
        template.publish(config.getWebsiteCrawlCompletedTopicName(), event);
    }

    public void pageCrawlCompletedEvent(String websiteCrawlId, PageSnapshot curVal) {
        PageCrawlCompletedEvent payload = new PageCrawlCompletedEvent(websiteCrawlId, curVal);
        log.debug("Page crawl completed. Publishing event. {}", payload);
        template.publish(config.getPageCrawlCompletedTopicName(), payload);
    }

    public void onCrawlStatusUpdate(CrawlStatusUpdateEvent event) {
        template.publish(config.getCrawlStatusUpdateTopicName(), event);
    }
}
