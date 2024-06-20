package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Data;
import lombok.Getter;

@Data
public class PageCrawledEvent {
    private final WebsiteCrawl websiteCrawl;
    private final CrawlResult crawlResult;
}