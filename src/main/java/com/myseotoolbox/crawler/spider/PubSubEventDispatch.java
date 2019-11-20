package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlCompletedEvent;
import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.model.PageSnapshot;
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

    public void websiteCrawlCompletedEvent(WebsiteCrawl websiteCrawl) {
        CrawlCompletedEvent payload = new CrawlCompletedEvent(websiteCrawl);
        log.info("WebsiteCrawl completed. Publishing event. {}", payload);
        template.publish(config.getWebsiteCrawlCompletedTopicName(), payload);
    }

    public void pageCrawlCompletedEvent(String websiteCrawlId, PageSnapshot curVal) {
        PageCrawlCompletedEvent payload = new PageCrawlCompletedEvent(websiteCrawlId, curVal);
        log.debug("Page crawl completed. Publishing event. {}", payload);
        template.publish(config.getPageCrawlCompletedTopicName(), payload);
    }
}
