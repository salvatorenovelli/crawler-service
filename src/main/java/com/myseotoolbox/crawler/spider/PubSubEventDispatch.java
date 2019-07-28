package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlCompletedEvent;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubEventDispatch {

    private final PubSubPublisherTemplate template;
    private final PubSubProperties config;

    public void crawlCompletedEvent(WebsiteCrawl websiteCrawl) {
        CrawlCompletedEvent payload = new CrawlCompletedEvent(websiteCrawl);
        log.info("Crawl completed. Publishing event. {}", payload);
        template.publish(config.getTopicName(), payload);
    }
}
