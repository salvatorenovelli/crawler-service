package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.sitemap.SitemapRepository;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageCrawledEventFactoryTest {

    private static final String TEST_ORIGIN = "http://origin";
    private static final PageSnapshot TEST_PAGE_SNAPSHOT = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri("http://host");
    private static final CrawlResult TEST_CRAWL_RESULT = CrawlResult.forSnapshot(TEST_PAGE_SNAPSHOT);
    private static final WebsiteCrawl CRAWL = TestWebsiteCrawlFactory.newWebsiteCrawlFor(TEST_ORIGIN, Collections.emptyList());

    @Mock private SitemapRepository sitemapRepository;
    private PageCrawledEventFactory sut;

    @BeforeEach
    void setUp() {
        sut = new PageCrawledEventFactory(sitemapRepository);
    }

    @Test
    void shouldMakeWIthCorrectData() {
        PageCrawledEvent event = sut.make(CRAWL, TEST_CRAWL_RESULT);

        assertThat(event.crawlResult(), is(TEST_CRAWL_RESULT));
        assertThat(event.websiteCrawl(), is(CRAWL));
    }

    @Test
    void shouldDecorateEventWithSitemapLinks() {

        URI expectedSitemapLink = URI.create("http://origin/sitempa.xml");

        when(sitemapRepository.findSitemapsLinkingTo(CRAWL, TEST_PAGE_SNAPSHOT.getUri()))
                .thenReturn(Set.of(expectedSitemapLink));

        PageCrawledEvent event = sut.make(CRAWL, TEST_CRAWL_RESULT);

        assertThat(event.sitemapInboundLinks(), IsCollectionWithSize.hasSize(1));
        assertThat(event.sitemapInboundLinks(), IsIterableContaining.hasItem(expectedSitemapLink));
    }
}