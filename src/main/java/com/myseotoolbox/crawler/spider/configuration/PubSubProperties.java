package com.myseotoolbox.crawler.spider.configuration;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RequiredArgsConstructor
@Data
@ConfigurationProperties(prefix = "publisher")
public class PubSubProperties {
    private String topicName;
    private int connectionTimeoutSeconds = 10;
}


