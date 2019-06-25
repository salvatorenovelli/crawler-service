package com.myseotoolbox.crawler.websitecrawl;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Collection;

@Document
@Data
public class WebsiteCrawl {
    @Id private final String id;
    @Indexed private final String origin;
    private final LocalDateTime createdAt;
    private final Collection<String> seeds;

    public static WebsiteCrawl fromCrawlStartedEvent(CrawlStartedEvent conf) {
        return new WebsiteCrawl(null, conf.getOrigin(), LocalDateTime.now(), conf.getSeeds());
    }
}
