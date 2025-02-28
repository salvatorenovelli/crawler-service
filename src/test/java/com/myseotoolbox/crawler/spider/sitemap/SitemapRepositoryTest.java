package com.myseotoolbox.crawler.spider.sitemap;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Set;

import static com.myseotoolbox.crawler.spider.sitemap.TestSitemapCrawlResultBuilder.aSitemapCrawlResultForOrigin;
import static com.myseotoolbox.testutils.TestWebsiteCrawlFactory.newWebsiteCrawlFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SitemapRepositoryTest {

    private SitemapRepository sut = new SitemapRepository();

    @Mock private Appender<ILoggingEvent> mockAppender;

    @BeforeEach
    void setUp() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(SitemapRepository.class.getName());
        logger.addAppender(mockAppender);
    }


    @Test
    void shouldPersistAndRetrieveSitemapCrawlResult() {
        SitemapCrawlResult sitemapCrawlResult = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain/sitemap.xml")
                .withLinks(URI.create("http://domain/page"))
                .build();

        sut.persist(sitemapCrawlResult);

        Set<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain/page");
        assertThat(result, contains(URI.create("http://domain/sitemap.xml")));
    }

    @Test
    void shouldGracefullyFallbackOnMissingCrawl() {
        WebsiteCrawl nonExistentCrawl = newWebsiteCrawlFor("http://domain", Set.of());
        assertThat(sut.findSitemapsLinkingTo(nonExistentCrawl, "http://domain/page"), is(empty()));
        verify(mockAppender).doAppend(argThat(
                argument -> argument.getLevel().equals(Level.ERROR) &&
                        argument.getMessage().contains("WebsiteCrawl {} was not present when finding links for {}")
        ));
    }


    @Test
    void shouldFindOnSitemapsWithMultipleLinks() {
        SitemapCrawlResult sitemapCrawlResult = aSitemapCrawlResultForOrigin("http://domain")
                .havingSitemapOn("http://domain/sitemap.xml")
                .withLinks(URI.create("http://domain/page1"), URI.create("http://domain/page2"))
                .build();

        sut.persist(sitemapCrawlResult);

        Set<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain/page1");
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

        Set<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult1.crawl(), "http://domain1/page1");
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

        Set<URI> result = sut.findSitemapsLinkingTo(sitemapCrawlResult.crawl(), "http://domain2/nonexisting");
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