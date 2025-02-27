package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;

import java.net.URI;
import java.util.Set;

public record PageCrawledEvent(WebsiteCrawl websiteCrawl, CrawlResult crawlResult, Set<URI> sitemapInboundLinks) {
}