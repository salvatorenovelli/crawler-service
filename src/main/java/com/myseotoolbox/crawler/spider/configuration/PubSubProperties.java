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
    private String websiteCrawlCompletedTopicName;
    private String pageCrawlCompletedTopicName;
    private int connectionTimeoutSeconds = 10;
}


