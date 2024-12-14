package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor
public class TestSitemapCrawlResultBuilder {
    private final WebsiteCrawl websiteCrawl;
    private List<URI> links = emptyList();


    public static TestSitemapCrawlResultBuilder aSitemapCrawlResultForOrigin(String origin) {
        var websiteCrawl = TestWebsiteCrawlFactory.newWebsiteCrawlFor(origin, singletonList(URI.create(origin)));
        return new TestSitemapCrawlResultBuilder(websiteCrawl);
    }

    public TestSitemapCrawlResultBuilder withLinks(URI... links) {
        this.links = Arrays.asList(links);
        return this;
    }

    public SitemapCrawlResult build() {
        return new SitemapCrawlResult(websiteCrawl, singletonList(new SiteMap(URI.create(websiteCrawl.getOrigin()), links)));
    }
}
