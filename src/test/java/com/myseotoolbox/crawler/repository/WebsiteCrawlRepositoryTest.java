package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.websitecrawl.CrawlTrigger;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.myseotoolbox.crawler.websitecrawl.CrawlTrigger.forUserInitiatedWorkspaceCrawl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataMongoTest
class WebsiteCrawlRepositoryTest {

    @Autowired
    private WebsiteCrawlRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldFindMostRecentCrawlForWorkspace() {
        repository.save(newWebsiteCrawlForWorkspace(123, Instant.EPOCH));
        repository.save(newWebsiteCrawlForWorkspace(123, Instant.EPOCH.plusSeconds(10)));

        Optional<WebsiteCrawl> result = repository.findLatestByWorkspace(123);

        assertThat(result.get().getStartedAt(), is(Instant.EPOCH.plusSeconds(10)));
    }

    @Test
    void shouldNotReturnCrawlForDifferentWorkspace() {
        repository.save(newWebsiteCrawlForWorkspace(123, Instant.EPOCH));
        repository.save(newWebsiteCrawlForWorkspace(456, Instant.EPOCH.plusSeconds(10)));

        Optional<WebsiteCrawl> result = repository.findLatestByWorkspace(123);

        assertThat(result.get().getStartedAt(), is(Instant.EPOCH));
    }

    @Test
    void shouldFindCrawlWhenMultipleWorkspacesInTrigger() {
        repository.save(newWebsiteCrawlForWorkspace(List.of(123, 456), Instant.EPOCH));
        repository.save(newWebsiteCrawlForWorkspace(List.of(123, 789), Instant.EPOCH.plusSeconds(10)));

        Optional<WebsiteCrawl> result = repository.findLatestByWorkspace(456);

        assertThat(result.get().getStartedAt(), is(Instant.EPOCH));
    }

    @Test
    void shouldReturnEmptyWhenNoWorkspaceIsPresent() {
        repository.save(newWebsiteCrawlForWorkspace(List.of(123, 456), Instant.EPOCH));

        Optional<WebsiteCrawl> result = repository.findLatestByWorkspace(1);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    void shouldNotConsiderEmptyStartDate() {
        repository.save(newWebsiteCrawlForWorkspace(123, Instant.EPOCH));
        repository.save(newWebsiteCrawlForWorkspace(123, null));

        Optional<WebsiteCrawl> result = repository.findLatestByWorkspace(123);

        assertThat(result.get().getStartedAt(), is(Instant.EPOCH));
    }

    private WebsiteCrawl newWebsiteCrawlForWorkspace(int workspaceNumber, Instant instant) {
        return WebsiteCrawl.builder()
                .trigger(forUserInitiatedWorkspaceCrawl(workspaceNumber))
                .startedAt(instant)
                .build();
    }

    private WebsiteCrawl newWebsiteCrawlForWorkspace(List<Integer> workspaceNumbers, Instant instant) {
        return WebsiteCrawl.builder()
                .trigger(CrawlTrigger.forScheduledCrawlTrigger(workspaceNumbers))
                .startedAt(instant)
                .build();
    }
}
