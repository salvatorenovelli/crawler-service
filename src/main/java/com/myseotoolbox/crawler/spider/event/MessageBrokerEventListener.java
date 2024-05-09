package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.spider.ratelimiter.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class MessageBrokerEventListener {

    private final PubSubPublisherTemplate template;
    private final PubSubProperties config;
    private final RateLimiterRepository rateLimiterRepository;

    @EventListener
    public void onWebsiteCrawlStarted(WebsiteCrawlStartedEvent event) {
        log.debug("WebsiteCrawl started `{}`. Publishing event.", event.getWebsiteCrawl().getId().toHexString());
        publishMessage(config.getWebsiteCrawlStartedTopicName(), event);
    }

    @EventListener
    public void onWebsiteCrawlCompletedEvent(WebsiteCrawlCompletedEvent event) {
        log.info("WebsiteCrawl completed. Publishing event. {}", event);
        publishMessage(config.getWebsiteCrawlCompletedTopicName(), event);
    }

    @EventListener
    public void onPageCrawlCompletedEvent(PageCrawledEvent event) {
        PageCrawlCompletedEvent payload = new PageCrawlCompletedEvent(event.getWebsiteCrawl().getId().toHexString(), event.getCrawlResult().getPageSnapshot());
        log.debug("Page crawl completed. Publishing event. {}", payload);
        publishMessage(config.getPageCrawlCompletedTopicName(), payload);
    }

    @EventListener
    public void onCrawlStatusUpdate(CrawlStatusUpdateEvent event) {
        getRateLimiter(event.getWebsiteCrawlId()).process(() -> {
            log.debug("Publishing Crawl Status update. Publishing event. {} on {}", event, config.getCrawlStatusUpdateConfiguration().getTopicName());
            publishMessage(config.getCrawlStatusUpdateConfiguration().getTopicName(), event);
            return null;
        });
    }

    private <T> void publishMessage(String topicName, T payload) {
        runOrLogWarning(() -> {
            template.publish(topicName, payload);
        }, "Error while publishing on topic " + topicName);
    }

    private RateLimiter getRateLimiter(String websiteCrawlId) {
        return rateLimiterRepository.getFor(websiteCrawlId);
    }


}
