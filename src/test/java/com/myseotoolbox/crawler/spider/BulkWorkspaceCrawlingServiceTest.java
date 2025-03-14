package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.CrawlDelayExpired;
import com.myseotoolbox.crawler.repository.WebsiteCrawlRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.utils.CurrentTimeUtils;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.MAX_CONCURRENT_CONNECTIONS;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BulkWorkspaceCrawlingServiceTest {


    private static final int YESTERDAY = -1;
    private static final int TWO_DAYS_AGO = -2;
    public static final int DEFAULT_CRAWL_VALUE_WHEN_MISSING = DefaultCrawlerSettings.MIN_CRAWL_INTERVAL;
    public static final int MAX_CONCURRENT_CRAWLS = 3;
    private final List<Tuple2<CrawlJobConfiguration, CrawlJob>> mockJobs = new ArrayList<>();
    private final List<Workspace> allWorkspaces = new ArrayList<>();

    @Mock private CrawlJobFactory crawlJobFactory;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private RobotsTxtAggregation robotsAggregation;
    @Mock private WebsiteCrawlRepository websiteCrawlRepository;

    BulkWorkspaceCrawlingService sut;
    @Spy private Executor executor = new CurrentThreadTestExecutorService();

    @Before
    public void setUp() {
         CrawlDelayExpired crawlDelayExpired = new CrawlDelayExpired(websiteCrawlRepository, new CurrentTimeUtils());

        sut = new BulkWorkspaceCrawlingService(workspaceRepository, crawlJobFactory, crawlDelayExpired, robotsAggregation, executor);

        when(crawlJobFactory.make(any())).thenAnswer(
                invocation -> {
                    CrawlJob mock = mock(CrawlJob.class);
                    CrawlJobConfiguration configuration = invocation.getArgument(0);
                    mockJobs.add(Tuple.of(configuration, mock));
                    return mock;
                }
        );

        when(workspaceRepository.findAll()).thenReturn(allWorkspaces);
        when(robotsAggregation.mergeConfigurations(any())).thenReturn(new EmptyRobotsTxt(null));
    }

    @Test
    public void shouldBuildRobotsTxtBasedOnAggregationAndUseItToConfigureCrawlJob() {
        RobotsTxt returnedValue = Mockito.mock(RobotsTxt.class);
        when(robotsAggregation.mergeConfigurations(any())).thenReturn(returnedValue);

        givenAWorkspace().withWebsiteUrl("http://host1/1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/2").build();
        givenAWorkspace().withWebsiteUrl("http://host1/3").build();

        sut.crawlAllWorkspaces();
        verify(robotsAggregation).mergeConfigurations(argThat(workspaces -> workspaces.size() == 3));
        verify(crawlJobFactory).make(argThat(argument -> argument.getRobotsTxt() == returnedValue));
    }

    @Test
    public void nullCrawlerSettingsDontStopOtherCrawls() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withCrawlerSettings(null).build();

        sut.crawlAllWorkspaces();

        verifyCrawlStartedFor("http://host1");
    }

    @Test
    public void shouldCrawlAllTheWorkspaces() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host2").build();

        sut.crawlAllWorkspaces();

        verifyCrawlStartedFor("http://host1");
        verifyCrawlStartedFor("http://host2");
    }

    @Test
    public void shouldNotCrawlTwice() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/").build();

        sut.crawlAllWorkspaces();

        verifyCrawlStartedFor("http://host1");
        verifyNoMoreCrawls();
    }

    @Test
    public void shouldGroupByOrigin() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2/").build();
        givenAWorkspace().withWebsiteUrl("http://host2").build();

        sut.crawlAllWorkspaces();

        crawlStartedForOriginWithSeeds("http://host1", asList("http://host1", "http://host1/path1/", "http://host1/path2/"));
        crawlStartedForOriginWithSeeds("http://host2", asList("http://host2"));
        verifyNoMoreCrawls();
    }

    @Test
    public void shouldNotHaveDuplicatesInSeeds() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2/").build();

        sut.crawlAllWorkspaces();

        crawlStartedForOriginWithSeeds("http://host1/", asList("http://host1", "http://host1/path1/", "http://host1/path2/"));
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
        for (int i = 0; i < MAX_CONCURRENT_CONNECTIONS * 2; i++) {
            givenAWorkspace().withWebsiteUrl("http://host1/path" + i).build();
        }

        sut.crawlAllWorkspaces();

        websiteCrawledWithConcurrentConnections(MAX_CONCURRENT_CONNECTIONS);
    }

    @Test
    public void numConnectionsOnlyCountsUniqueSeeds() {
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1/").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2/").build();

        sut.crawlAllWorkspaces();

        websiteCrawledWithConcurrentConnections(2);
    }

    @Test
    public void exceptionInBuildOrStartShouldNotPreventOtherCrawls() {
        String originWithException = "http://host1/";
        givenAWorkspace().withWebsiteUrl(originWithException).build();
        givenAWorkspace().withWebsiteUrl("http://host2/").build();

        when(crawlJobFactory.make(argThat(argument -> argument.getOrigin().equals(URI.create(originWithException))))).thenThrow(new RuntimeException("Testing exceptions"));

        sut.crawlAllWorkspaces();

        verifyCrawlStartedFor("http://host2/");
    }

    @Test
    public void shouldOnlyCrawlWhereCrawlingIsEnabled() {
        givenAWorkspace().withWebsiteUrl("http://host2/").withCrawlingDisabled().build();
        sut.crawlAllWorkspaces();
        verifyNoMoreCrawls();
    }


    @Test
    public void shouldConsiderPathForCrawlInterval() {
        givenAWorkspace().withWebsiteUrl("http://host1/abc/").withCrawlingIntervalOf(2).withLastCrawlHappened(YESTERDAY).build();
        givenAWorkspace().withWebsiteUrl("http://host1/cde/").withCrawlingIntervalOf(1).withLastCrawlHappened(YESTERDAY).build();

        sut.crawlAllWorkspaces();
        verifyCrawlStartedFor("http://host1/cde/");
        verifyNoMoreCrawls();
    }

    @Test
    public void shouldOnlyCrawlAtConfiguredInterval() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(1).withLastCrawlHappened(YESTERDAY).build();
        givenAWorkspace().withWebsiteUrl("http://host2/").withCrawlingIntervalOf(2).withLastCrawlHappened(TWO_DAYS_AGO).build();
        givenAWorkspace().withWebsiteUrl("http://host3/").withCrawlingIntervalOf(3).withLastCrawlHappened(TWO_DAYS_AGO).build();
        givenAWorkspace().withWebsiteUrl("http://host4/").withCrawlingIntervalOf(3).withLastCrawlHappened(YESTERDAY).build();

        sut.crawlAllWorkspaces();

        verifyCrawlStartedFor("http://host1/");
        verifyCrawlStartedFor("http://host2/");
        verifyNoMoreCrawls();
    }

    @Test
    public void canHandleNoLastCrawl() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(1).build();
        sut.crawlAllWorkspaces();
        verifyCrawlStartedFor("http://host1/");
    }

    @Test
    public void canHandleNoCrawlIntervalSpecified() {
        givenAWorkspace().withWebsiteUrl("http://host1/").withCrawlingIntervalOf(DEFAULT_CRAWL_VALUE_WHEN_MISSING).withLastCrawlHappened(YESTERDAY).build();
        sut.crawlAllWorkspaces();
        verifyCrawlStartedFor("http://host1/");
    }

    @Test
    public void shouldExecuteEveryCrawlInADifferentThread() {
        givenAWorkspace().withWebsiteUrl("http://host1").withCrawlingIntervalOf(1).build();
        givenAWorkspace().withWebsiteUrl("http://host2").withCrawlingIntervalOf(1).build();

        sut.crawlAllWorkspaces();

        verify(executor, times(2)).execute(any());
    }

    private void websiteCrawledWithConcurrentConnections(int numConnections) {
        verify(crawlJobFactory).make(argThat(argument -> argument.getMaxConcurrentConnections() == numConnections));
    }

    private void verifyCrawlStartedFor(String origin) {
        crawlStartedForOriginWithSeeds(addTrailingSlashIfMissing(origin), singletonList(origin));
    }

    private synchronized void crawlStartedForOriginWithSeeds(String origin, List<String> seeds) {
        List<URI> expectedSeeds = seeds.stream().map(URI::create).toList();

        try {
            URI originRoot = create(origin).resolve("/");

            verify(crawlJobFactory).make(argThat(conf ->
                    conf.getOrigin().equals(originRoot) &&
                            conf.getSeeds().containsAll(expectedSeeds)));

            mockJobs.stream().filter(tuple -> tuple._1 != null && tuple._1.getOrigin().equals(originRoot)).map(t -> t._2).forEach(job -> verify(job, times(1)).run());
        } catch (Throwable e) {
            System.out.println(mockingDetails(crawlJobFactory).printInvocations());
            throw e;
        }
    }

    private synchronized void verifyNoMoreCrawls() {
        try {
            verifyNoMoreInteractions(crawlJobFactory);
            mockJobs.stream().map(Tuple2::_2).forEach(Mockito::verifyNoMoreInteractions);
        } catch (Throwable e) {
            System.out.println(mockingDetails(crawlJobFactory).printInvocations());
            mockJobs.stream().map(Tuple2::_2).forEach(crawlJob -> System.out.println(mockingDetails(crawlJob).printInvocations()));
            throw e;
        }
    }

    private TestWorkspaceBuilder givenAWorkspace() {
        return new TestWorkspaceBuilder(allWorkspaces, websiteCrawlRepository);
    }

    private String addTrailingSlashIfMissing(String uri) {
        return uri + (uri.endsWith("/") ? "" : "/");
    }
}