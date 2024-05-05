package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsConfiguration {

    @Bean
    public RateLimiterRepository getRateLimiterRepository(PubSubProperties pubSubProperties, ClockUtils clockUtils) {
        return new RateLimiterRepository(pubSubProperties.getCrawlStatusUpdateConfiguration().getTopicPublishMinIntervalMillis(), clockUtils, 500);
    }
}
