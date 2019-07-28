package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlCompletedEvent;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PubSubEventDispatch {

    private final PubSubPublisherTemplate template;
    private final PubSubProperties config;

    public void crawlCompletedEvent(WebsiteCrawl websiteCrawl) {
        template.publish(config.getTopicName(), new CrawlCompletedEvent(websiteCrawl));
    }
}
