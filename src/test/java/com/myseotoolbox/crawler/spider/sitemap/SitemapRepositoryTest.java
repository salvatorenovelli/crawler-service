package com.myseotoolbox.crawler.spider.sitemap;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static com.myseotoolbox.crawler.spider.sitemap.TestSitemapCrawlResultBuilder.aSitemapCrawlResultForOrigin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

class SitemapRepositoryTest {

    private SitemapRepository sut = new SitemapRepository();


    @Test
    void shouldPersistAndRetrieveSitemapCrawlResult() {
        SitemapCrawlResult sitemapCrawlResult = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain/sitemap.xml")
                .withLinks(URI.create("http://domain/page"))
                .build();

        sut.persist(sitemapCrawlResult);

        List<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain/page");
        assertThat(result, contains(URI.create("http://domain/sitemap.xml")));
    }

    @Test
    void shouldFindOnSitemapsWithMultipleLinks() {
        SitemapCrawlResult sitemapCrawlResult = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain/sitemap.xml")
                .withLinks(URI.create("http://domain/page1"), URI.create("http://domain/page2"))
                .build();

        sut.persist(sitemapCrawlResult);

        List<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain/page1");
        assertThat(result, containsInAnyOrder(URI.create("http://domain/sitemap.xml")));

        result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain/page2");
        assertThat(result, containsInAnyOrder(URI.create("http://domain/sitemap.xml")));
    }

    @Test
    void shouldFilterByWebsiteCrawl() {
        SitemapCrawlResult sitemapCrawlResult1 = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain1/sitemap.xml")
                .withLinks(URI.create("http://domain1/page1"))
                .build();

        SitemapCrawlResult sitemapCrawlResult2 = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain1/sitemap2.xml")
                .withLinks(URI.create("http://domain1/page1"))
                .build();

        sut.persist(sitemapCrawlResult1);
        sut.persist(sitemapCrawlResult2);

        List<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult1.crawl(), "http://domain1/page1");
        assertThat(result, containsInAnyOrder(URI.create("http://domain1/sitemap.xml")));

        result = sut.findSitemapsLinkingTo(sitemapCrawlResult2.crawl(), "http://domain1/page1");
        assertThat(result, containsInAnyOrder(URI.create("http://domain1/sitemap2.xml")));
    }

    @Test
    void shouldFilterByUri() {
        SitemapCrawlResult sitemapCrawlResult = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain/sitemap.xml")
                .withLinks(URI.create("http://domain/page1"), URI.create("http://domain/page2"))
                .build();

        sut.persist(sitemapCrawlResult);

        List<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain2/nonexisting");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenPersistingDuplicateCrawl() {
        SitemapCrawlResult sitemapCrawlResult = TestSitemapCrawlResultBuilder
                .aSitemapCrawlResultForOrigin("http://domain/sitemap.xml")
                .build();

        sut.persist(sitemapCrawlResult);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                sut.persist(sitemapCrawlResult)
        );

        assertTrue(exception.getMessage().contains("Crawl already exists"));
    }

    @Test
    void shouldPurgeCrawls() {
        SitemapCrawlResult sitemapCrawlResult = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain/sitemap.xml")
                .withLinks(URI.create("http://domain/page"))
                .build();

        sut.persist(sitemapCrawlResult);
        SitemapCrawlResult removed = sut.purgeCrawl(sitemapCrawlResult.crawl());

        assertEquals(sitemapCrawlResult, removed);
        assertTrue(sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain/page").isEmpty());
    }
}