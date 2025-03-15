package com.myseotoolbox.crawler.websitecrawl;

import org.bson.types.ObjectId;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

public class WebsiteCrawlFactory {

    public static WebsiteCrawl newWebsiteCrawlFor(String owner, CrawlTrigger trigger, String origin, Collection<URI> seeds) {
        return new WebsiteCrawl(new ObjectId(), owner, trigger, origin, null, seeds.stream().map(URI::toString).collect(Collectors.toList()));
    }
}
