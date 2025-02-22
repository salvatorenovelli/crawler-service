package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

@RequiredArgsConstructor
public class TestSitemapCrawlResultBuilder {
    private final WebsiteCrawl websiteCrawl;
    private Set<URI> curLinks = emptySet();
    private URI currentSitemapLocation;
    private List<SiteMap> sitemaps = new ArrayList<>();


    public static TestSitemapCrawlResultBuilder aSitemapCrawlResultForOrigin(String origin) {
        var websiteCrawl = TestWebsiteCrawlFactory.newWebsiteCrawlFor(origin, singletonList(URI.create(origin)));
        return new TestSitemapCrawlResultBuilder(websiteCrawl);
    }

    public static TestSitemapCrawlResultBuilder aSitemapCrawlResultForCrawl(WebsiteCrawl crawl) {
        return new TestSitemapCrawlResultBuilder(crawl);
    }

    public TestSitemapCrawlResultBuilder havingSitemapOn(String curSitemapLocation) {
        this.currentSitemapLocation = URI.create(curSitemapLocation);
        return this;
    }

    public TestSitemapCrawlResultBuilder withLinks(URI... links) {
        Assert.notNull(currentSitemapLocation, "You need to set sitemap location first. havingSitemapOn(...)");
        this.curLinks = Set.of(links);
        return this;
    }

    public SitemapCrawlResult build() {
        and();
        return new SitemapCrawlResult(websiteCrawl, sitemaps);
    }

    public TestSitemapCrawlResultBuilder and() {
        sitemaps.add(new SiteMap(currentSitemapLocation, curLinks));
        currentSitemapLocation = null;
        curLinks = emptySet();
        return this;
    }
}
