package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;

import java.net.URI;
import java.util.Set;

@Data
public class PageCrawledEvent {
    private final WebsiteCrawl websiteCrawl;
    private final CrawlResult crawlResult;
    private final Set<URI> sitemapInboundLinks;
}