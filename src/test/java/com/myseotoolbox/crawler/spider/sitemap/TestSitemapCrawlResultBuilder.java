package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor
public class TestSitemapCrawlResultBuilder {
    private final WebsiteCrawl websiteCrawl;
    private Set<URI> links = emptySet();


    public static TestSitemapCrawlResultBuilder aSitemapCrawlResultForOrigin(String origin) {
        var websiteCrawl = TestWebsiteCrawlFactory.newWebsiteCrawlFor(origin, singletonList(URI.create(origin)));
        return new TestSitemapCrawlResultBuilder(websiteCrawl);
    }

    public TestSitemapCrawlResultBuilder withLinks(URI... links) {
        this.links = Set.of(links);
        return this;
    }

    public SitemapCrawlResult build() {
        return new SitemapCrawlResult(websiteCrawl, singletonList(new SiteMap(URI.create(websiteCrawl.getOrigin()), links)));
    }
}
