package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.spider.sitemap.SitemapRepository;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PageCrawledEventFactory {

    private final SitemapRepository sitemapRepository;

    public PageCrawledEvent make(WebsiteCrawl websiteCrawl, CrawlResult crawlResult) {
        List<URI> sitemapLinks = sitemapRepository.findSitemapsLinkingTo(websiteCrawl, crawlResult.getUri());
        return new PageCrawledEvent(websiteCrawl, crawlResult, sitemapLinks);
    }
}