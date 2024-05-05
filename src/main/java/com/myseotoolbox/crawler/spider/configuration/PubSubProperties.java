package com.myseotoolbox.crawler.spider.configuration;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ConfigurationProperties(prefix = "publisher")
public class PubSubProperties {
    private String websiteCrawlStartedTopicName;
    private String websiteCrawlCompletedTopicName;
    private String pageCrawlCompletedTopicName;
    private TopicConfiguration crawlStatusUpdateConfiguration;
    private int connectionTimeoutSeconds = 10;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopicConfiguration {
        private String topicName;
        private int topicPublishMinIntervalMillis;
    }
}