package com.myseotoolbox.crawler.websitecrawl;

import org.bson.types.ObjectId;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

public class WebsiteCrawlFactory {

    public static WebsiteCrawl newWebsiteCrawlFor(String origin, Collection<URI> seeds) {
        return newWebsiteCrawlFor(new ObjectId(), origin, seeds);
    }

    public static WebsiteCrawl newWebsiteCrawlFor(ObjectId crawlId, String origin, Collection<URI> seeds) {
        return new WebsiteCrawl(crawlId, origin, LocalDateTime.now(), seeds.stream().map(URI::toString).collect(Collectors.toList()));
    }
}
