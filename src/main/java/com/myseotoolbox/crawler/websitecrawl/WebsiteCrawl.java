package com.myseotoolbox.crawler.websitecrawl;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Collection;

@Document
@Data
public class WebsiteCrawl {
    @Id private final ObjectId id;
    @Indexed private final String origin;
    private final LocalDateTime createdAt;
    private final Collection<String> seeds;

    WebsiteCrawl(ObjectId id, String origin, LocalDateTime createdAt, Collection<String> seeds) {
        this.id = id;
        this.origin = origin;
        this.createdAt = createdAt;
        this.seeds = seeds;
    }

    public static WebsiteCrawl fromCrawlStartedEvent(ObjectId crawlId, CrawlStartedEvent conf) {
        return new WebsiteCrawl(crawlId, conf.getOrigin(), LocalDateTime.now(), conf.getSeeds());
    }
}
