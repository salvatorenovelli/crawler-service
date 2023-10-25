package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Getter;

@Getter
public class PageCrawledEvent {

    private final WebsiteCrawl websiteCrawl;
    private final CrawlResult crawlResult;

    public PageCrawledEvent(WebsiteCrawl websiteCrawl, CrawlResult crawlResult) {
        this.websiteCrawl = websiteCrawl;
        this.crawlResult = crawlResult;
    }

    public static PageCrawledEvent from(WebsiteCrawl websiteCrawl, CrawlResult crawlResult) {
        return new PageCrawledEvent(websiteCrawl, crawlResult);
    }
}
