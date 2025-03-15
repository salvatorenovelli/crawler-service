package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.websitecrawl.CrawlTrigger;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrawlDelayExpiredTest {

    @Mock
    private WebsiteCrawlRepository repository;

    private CrawlDelayExpired crawlDelayExpired;
    private final WebsiteCrawl LAST_CRAWL = mockWebsiteCrawl(123, Instant.EPOCH);
    private TestTimeUtils timeUtils = new TestTimeUtils();


    private static final Workspace TEST_WORKSPACE = Workspace.builder()
            .seqNumber(123)
            .crawlerSettings(CrawlerSettings.builder()
                    .crawlIntervalDays(7)
                    .build())
            .build();

    @BeforeEach
    void setUp() {
        this.crawlDelayExpired = new CrawlDelayExpired(repository, timeUtils);
        when(repository.findLatestByWorkspace(123)).thenReturn(Optional.of(LAST_CRAWL));

    }

    @Test
    void shouldReturnTrueIfNoPreviousCrawlExists() {
        when(repository.findLatestByWorkspace(123)).thenReturn(Optional.empty());
        boolean result = crawlDelayExpired.isCrawlDelayExpired(TEST_WORKSPACE);
        assertThat(result, is(true));
    }

    @Test
    void shouldReturnFalseIfCrawlDelayNotExpired() {
        timeUtils.addDays(5);
        boolean result = crawlDelayExpired.isCrawlDelayExpired(TEST_WORKSPACE);
        assertThat(result, is(false));
    }

    @Test
    void shouldReturnTrueIfCrawlDelayExpired() {
        timeUtils.addDays(7);
        boolean result = crawlDelayExpired.isCrawlDelayExpired(TEST_WORKSPACE);
        assertThat(result, is(true));
    }

    private WebsiteCrawl mockWebsiteCrawl(int workspaceNumber, Instant instant) {
        return WebsiteCrawl.builder()
                .trigger(CrawlTrigger.forUserInitiatedWorkspaceCrawl(workspaceNumber))
                .startedAt(instant)
                .build();
    }
}
