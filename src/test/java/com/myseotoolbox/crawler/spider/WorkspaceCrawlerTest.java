package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlerSettings;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.myseotoolbox.crawler.spider.WorkspaceCrawler.MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN;
import static java.net.URI.create;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceCrawlerTest {


    private static final int YESTERDAY = -1;
    private static final int TWO_DAYS_AGO = -2;
    public static final int DEFAULT_CRAWL_VALUE_WHEN_MISSING = CrawlerSettings.MIN_CRAWL_INTERVAL;
    private final List<CrawlJob> mockJobs = new ArrayList<>();
    private final List<Workspace> allWorkspaces = new ArrayList<>();
    private final List<WebsiteCrawlLog> crawlLogs = new ArrayList<>();

    @Mock private CrawlJobFactory crawlFactory;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WebsiteCrawlLogRepository websiteCrawlLogRepository;

    WorkspaceCrawler sut;

    @Before
    public void setUp() {
        sut = new WorkspaceCrawler(workspaceRepository, crawlFactory, websiteCrawlLogRepository);

        when(crawlFactory.build(any(URI.class), anyList(), anyInt())).thenAnswer(
                invocation -> {
                    CrawlJob mock = mock(CrawlJob.class);
                    mockJobs.add(mock);
                    return mock;
                }
        );

        when(workspaceRepository.findAll()).thenReturn(allWorkspaces);

        when(websiteCrawlLogRepository
                .findTopByOriginOrderByDateDesc(anyString()))
                .thenAnswer(invocation -> crawlLogs.stream()
                        .filter(websiteCrawlLog -> websiteCrawlLog.getOrigin().equals(invocation.getArgument(0)))
                        .findFirst());

    }

    @Test
    public void shouldCrawlAllTheWorkspaces() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host2").build();

        sut.crawlAllWorkspaces();

        crawlStartedFor("http://host1");
        crawlStartedFor("http://host2");
    }


    @Test
    public void shouldNotCrawlTwice() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1").build();


        sut.crawlAllWorkspaces();

        crawlStartedFor("http://host1");
        verifyNoMoreCrawls();
    }


    @Test
    public void shouldGroupByOrigin() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2").build();
        givenAWorkspace().withWebsiteUrl("http://host2").build();

        sut.crawlAllWorkspaces();

        crawlStartedForOriginWithSeeds("http://host1", Arrays.asList("http://host1", "http://host1/path1", "http://host1/path2"));
        crawlStartedForOriginWithSeeds("http://host2", Arrays.asList("http://host2"));
    }


    @Test
    public void shouldNotHaveDuplicatesInSeeds() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2").build();

        sut.crawlAllWorkspaces();

        crawlStartedForOriginWithSeeds("http://host1", Arrays.asList("http://host1", "http://host1/path1", "http://host1/path2"));
        verifyNoMoreCrawls();
    }

    @Test
    public void shouldNotTryToBuildInvalidUrl() {
        givenAWorkspace().withWebsiteUrl("TBD").build();

        sut.crawlAllWorkspaces();

        verifyNoMoreCrawls();
    }

    @Test
    public void numConnectionsIsGreaterIfWeHaveMultipleSeeds() {
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2").build();

        sut.crawlAllWorkspaces();

        websiteCrawledWithConcurrentConnections(2);
    }

    @Test
    public void shouldNeverUseMoreThanMaxConnections() {
        for (int i = 0; i < MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN * 2; i++) {
            givenAWorkspace().withWebsiteUrl("http://host1/path" + i).build();
        }

        sut.crawlAllWorkspaces();

        websiteCrawledWithConcurrentConnections(MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN);

    }

    @Test
    public void numConnectionsOnlyCountsUniqueSeeds() {

        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2").build();

        sut.crawlAllWorkspaces();


        websiteCrawledWithConcurrentConnections(2);
    }

    @Test
    public void exceptionInBuildOrStartShouldNotPreventOtherCrawls() {
        String originWithException = "http://host1/";

        givenAWorkspace().withWebsiteUrl(originWithException).build();
        givenAWorkspace().withWebsiteUrl("http://host2/").build();


        when(crawlFactory.build(eq(create(originWithException)), anyList(), anyInt())).thenThrow(new RuntimeException("Testing exceptions"));

        sut.crawlAllWorkspaces();

        crawlStartedFor("http://host2");
    }

    @Test
    public void shouldOnlyCrawlWhereCrawlingIsEnabled() {
        givenAWorkspace().withWebsiteUrl("http://host2/").withCrawlingDisabled().build();
        sut.crawlAllWorkspaces();
        verifyNoMoreCrawls();
    }


    @Test
    public void shouldOnlyCrawlAtConfiguredInterval() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(1).withLastCrawlHappened(YESTERDAY).build();
        givenAWorkspace().withWebsiteUrl("http://host2/").withCrawlingIntervalOf(2).withLastCrawlHappened(TWO_DAYS_AGO).build();
        givenAWorkspace().withWebsiteUrl("http://host3/").withCrawlingIntervalOf(3).withLastCrawlHappened(TWO_DAYS_AGO).build();
        givenAWorkspace().withWebsiteUrl("http://host4/").withCrawlingIntervalOf(3).withLastCrawlHappened(YESTERDAY).build();

        sut.crawlAllWorkspaces();

        crawlStartedFor("http://host1");
        crawlStartedFor("http://host2");

        verifyNoMoreCrawls();
    }

    @Test
    public void canHandleNoLastCrawl() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(1).build();
        sut.crawlAllWorkspaces();
        crawlStartedFor("http://host1");
    }

    @Test
    public void canHandleNoCrawlIntervalSpecified() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(DEFAULT_CRAWL_VALUE_WHEN_MISSING).withLastCrawlHappened(YESTERDAY).build();
        sut.crawlAllWorkspaces();
        crawlStartedFor("http://host1");
    }

    @Test
    public void shouldPersistLastCrawl() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(1).build();
        sut.crawlAllWorkspaces();
        verify(websiteCrawlLogRepository).save(argThat(argument -> argument.getOrigin().equals("http://host1/") && argument.getDate() != null));
    }

    private void websiteCrawledWithConcurrentConnections(int numConnections) {
        verify(crawlFactory).build(any(URI.class), anyList(), eq(numConnections));
    }

    private void crawlStartedFor(String origin) {
        crawlStartedForOriginWithSeeds(origin, singletonList(origin));
    }


    private void crawlStartedForOriginWithSeeds(String origin, List<String> seeds) {
        Object[] expectedSeeds = seeds.stream().map(this::addTrailingSlashIfMissing).map(URI::create).toArray();

        verify(crawlFactory).build(eq(create(origin).resolve("/")),
                argThat(argument -> new HamcrestArgumentMatcher<>(containsInAnyOrder(expectedSeeds)).matches(argument)),
                ArgumentMatchers.anyInt());

        mockJobs.forEach(job -> verify(job).start());
    }

    private void verifyNoMoreCrawls() {
        verifyNoMoreInteractions(crawlFactory);
        mockJobs.forEach(Mockito::verifyNoMoreInteractions);
    }

    private WorkspaceBuilder givenAWorkspace() {
        return new WorkspaceBuilder();
    }

    private class WorkspaceBuilder {


        private final Workspace curWorkspace;

        private WorkspaceBuilder() {
            curWorkspace = new Workspace();
            curWorkspace.setCrawlerSettings(new CrawlerSettings(1, true, 1));
        }

        public WorkspaceBuilder withWebsiteUrl(String s) {
            curWorkspace.setWebsiteUrl(s);
            return this;
        }

        public void build() {
            allWorkspaces.add(curWorkspace);
        }

        public WorkspaceBuilder withCrawlingDisabled() {
            CrawlerSettings s = curWorkspace.getCrawlerSettings();
            curWorkspace.setCrawlerSettings(new CrawlerSettings(s.getMaxConcurrentConnections(), false, s.getCrawlIntervalDays()));
            return this;
        }

        public WorkspaceBuilder withCrawlingIntervalOf(int days) {
            CrawlerSettings s = curWorkspace.getCrawlerSettings();
            curWorkspace.setCrawlerSettings(new CrawlerSettings(s.getMaxConcurrentConnections(), s.isCrawlEnabled(), days));
            return this;
        }

        public WorkspaceBuilder withLastCrawlHappened(int dayOffset) {
            crawlLogs.add(new WebsiteCrawlLog(curWorkspace.getWebsiteUrl(), LocalDate.now().plusDays(dayOffset)));
            return this;
        }
    }

    private String addTrailingSlashIfMissing(String uri) {
        return uri + (uri.endsWith("/") ? "" : "/");
    }
}