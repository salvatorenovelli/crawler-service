package com.myseotoolbox.crawler.spider;


import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.pagelinks.OutboundLinkRepository;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.spider.event.CrawlStatusUpdateEvent;
import com.myseotoolbox.crawler.spider.event.MessageBrokerEventListener;
import com.myseotoolbox.crawler.spider.event.WebsiteCrawlCompletedEvent;
import com.myseotoolbox.crawler.spider.sitemap.SitemapRepository;
import com.myseotoolbox.crawler.testutils.TestCrawlJobBuilder;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import com.myseotoolbox.testutils.TimeUtilsTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.CrawlStatusUpdateEventMatcherBuilder.aCrawlStatusUpdateEvent;
import static com.myseotoolbox.crawler.spider.PageCrawledEventMatcherBuilder.aPageCrawledEvent;
import static com.myseotoolbox.crawler.testutils.TestCrawlJobBuilder.EXPECTED_WORKSPACES_FOR_TRIGGER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@Import(TimeUtilsTestConfiguration.class)
@SpringBootTest
public class CrawlEventsIntegrationTest {


    TestCrawlJobBuilder testCrawlJobBuilder;
    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    @Autowired MonitoredUriRepository monitoredUriRepository;
    @Autowired OutboundLinkRepository outboundLinkRepository;
    @Autowired private CrawlEventDispatchFactory factory;
    @Autowired SitemapRepository sitemapRepository;
    @Autowired PubSubProperties pubSubProperties;
    @SpyBean MessageBrokerEventListener messageBrokerEventListener;
    @MockBean PubSubPublisherTemplate template;


    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
        testCrawlJobBuilder = new TestCrawlJobBuilder(factory);
        testCrawlJobBuilder.setSitemapRepository(sitemapRepository);
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
        monitoredUriRepository.deleteAll();
    }

    @Test
    public void shouldNotifyOfCrawlStarted() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        verify(messageBrokerEventListener).onWebsiteCrawlStarted(
                argThat(event -> event.getWebsiteCrawl().getId().toHexString().equals(job.getWebsiteCrawlId()))
        );
    }

    @Test
    public void shouldNotifyOfPageCrawled() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        verify(messageBrokerEventListener).onPageCrawlCompletedEvent(
                aPageCrawledEvent().forCrawlId(job.getWebsiteCrawlId()).withPageSnapshotUri(getTestUri("/")).build()
        );
        verify(messageBrokerEventListener).onPageCrawlCompletedEvent(
                aPageCrawledEvent().forCrawlId(job.getWebsiteCrawlId()).withPageSnapshotUri(getTestUri("/abc")).build()
        );
        verify(messageBrokerEventListener).onPageCrawlCompletedEvent(
                aPageCrawledEvent().forCrawlId(job.getWebsiteCrawlId()).withPageSnapshotUri(getTestUri("/cde")).build()
        );
        verify(messageBrokerEventListener, times(3)).onPageCrawlCompletedEvent(any());
    }

    @Test
    public void pageCrawledShouldHaveSitemapInboundLinks() {
        givenAWebsite()
                .havingPage("/no_ahref_links")
                .withSitemapOn("/").havingUrls("/no_ahref_links").build()
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        verify(messageBrokerEventListener).onPageCrawlCompletedEvent(
                aPageCrawledEvent().forCrawlId(job.getWebsiteCrawlId())
                        .withPageSnapshotUri(getTestUri("/no_ahref_links"))
                        .withSitemapInboundLinks(getTestUri("/sitemap.xml")).build()
        );
    }


    @Test
    public void shouldNotifyOfCrawlProgress() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        //As the test is single threaded by design (included the executor in the queue), this works like a recursive algorithm
        //which goes depth first - so the first update gets published after the highest depth is discovered
        verify(messageBrokerEventListener).onCrawlStatusUpdate(aCrawlStatusUpdateEvent()
                .withCrawlId(job.getWebsiteCrawlId())
                .withVisited(2)
                .build());

        //When this is single threaded, the enqueueing becomes recursive. so this will get called twice with 3.
        //the first time for the last crawled page, the second time is for the first time it's called
        verify(messageBrokerEventListener, times(2)).onCrawlStatusUpdate(aCrawlStatusUpdateEvent()
                .withCrawlId(job.getWebsiteCrawlId())
                .withVisited(3)
                .build());

        //given this test is single threaded, this is deterministic. We only have 1 pending because with a single thread the crawler will go depth first
        //Bit tied with implementation details but don't think it's worth making it more generic
        verify(messageBrokerEventListener).onCrawlStatusUpdate(aCrawlStatusUpdateEvent().withPending(1).build());

        verify(messageBrokerEventListener, times(3)).onCrawlStatusUpdate(any());
    }

    @Test
    public void shouldEnqueueLinksBeforePublishingStatusUpdate() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        //this site has three pages. Pending is never 0 unless three pages have been visited
        verify(messageBrokerEventListener, never()).onCrawlStatusUpdate(aCrawlStatusUpdateEvent()
                .withCrawlId(job.getWebsiteCrawlId())
                .withVisitedBetween(0, 2)
                .withPending(0)
                .build());
    }

    @Test
    public void shouldNotifyOfWebsiteCrawlCompleted() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        verify(messageBrokerEventListener).onWebsiteCrawlCompletedEvent(new WebsiteCrawlCompletedEvent(job.getWebsiteCrawl(), 3, Instant.EPOCH));
    }

    @Test
    public void exceptionInOneTopicDoNotStopPublishingOtherEvents() {
        doThrow(new RuntimeException("This should not prevent update of the other"))
                .when(template).publish(eq(pubSubProperties.getPageCrawlCompletedTopicName()), any(PageCrawlCompletedEvent.class));

        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        verify(template, times(3)).publish(eq(pubSubProperties.getCrawlStatusUpdateConfiguration().getTopicName()), any(CrawlStatusUpdateEvent.class));
        verify(template).publish(eq(pubSubProperties.getWebsiteCrawlCompletedTopicName()), any(WebsiteCrawlCompletedEvent.class));
    }

    @Test
    public void shouldIncludeTriggerInEvents() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.run();

        verify(messageBrokerEventListener).onWebsiteCrawlStarted(
                argThat(event -> {
                    assertThat(event.getWebsiteCrawl().getTrigger().getTargetWorkspaces(), containsInAnyOrder(EXPECTED_WORKSPACES_FOR_TRIGGER.toArray()));
                    return true;
                })
        );
    }

    private String getTestUri(String path) {
        return testWebsiteBuilder.buildTestUri(path).toString();
    }

    private String getOrigin() {
        return testWebsiteBuilder.getBaseUriAsString();
    }

    private List<URI> testSeeds(String... urls) {
        return Arrays.stream(urls).map(s -> testWebsiteBuilder.buildTestUri(s)).collect(Collectors.toList());
    }

    private CrawlJob buildForSeeds(List<URI> uris) {
        return testCrawlJobBuilder.buildForSeeds(uris);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }
}
