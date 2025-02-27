package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SitemapRepository {
    private final Map<WebsiteCrawl, SitemapCrawlResult> sitemaps = new HashMap<>();

    public void persist(SitemapCrawlResult sitemapCrawlResult) {
        log.debug("Persisting sitemap crawl result: {}", sitemapCrawlResult);
        if (sitemaps.containsKey(sitemapCrawlResult.crawl())) {
            throw new IllegalArgumentException("Crawl already exists: " + sitemapCrawlResult.crawl());
        }
        sitemaps.put(sitemapCrawlResult.crawl(), sitemapCrawlResult);
    }

    public Set<URI> findSitemapsLinkingTo(WebsiteCrawl websiteCrawl, String uriToSearch)  {
        SitemapCrawlResult sitemapCrawlResult = sitemaps.get(websiteCrawl);

        if (sitemapCrawlResult == null) {
            log.error("WebsiteCrawl {} was not present when finding links for {}", websiteCrawl, uriToSearch);
            return Collections.emptySet(); //graceful fallback
        }

        return sitemapCrawlResult.sitemaps().stream()
                .filter(siteMap -> siteMap.links().contains(URI.create(uriToSearch)))
                .map(SiteMap::location)
                .collect(Collectors.toSet());
    }

    public SitemapCrawlResult purgeCrawl(WebsiteCrawl websiteCrawl) {
        return sitemaps.remove(websiteCrawl);
    }
}