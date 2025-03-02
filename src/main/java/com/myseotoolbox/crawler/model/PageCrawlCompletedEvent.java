package com.myseotoolbox.crawler.model;

import lombok.Data;
import java.net.URI;
import java.util.Set;

@Data
public class PageCrawlCompletedEvent {
    private final String websiteCrawlId;
    private final PageSnapshot curVal;
    private final Set<URI> sitemapInboundLinks;
}
