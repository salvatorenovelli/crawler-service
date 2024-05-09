package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.model.PageCrawlCompletedEvent;
import com.myseotoolbox.crawler.pagelinks.OutboundLinkRepository;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.spider.configuration.PubSubProperties;
import com.myseotoolbox.crawler.spider.event.WebsiteCrawlCompletedEvent;
import com.myseotoolbox.crawler.spider.event.CrawlStatusUpdateEvent;
import com.myseotoolbox.crawler.spider.event.MessageBrokerEventListener;
import com.myseotoolbox.crawler.testutils.TestCrawlJobBuilder;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.gcp.pubsub.core.publisher.PubSubPublisherTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.CrawlStatusUpdateEventMatcherBuilder.aCrawlStatusUpdateEvent;
import static com.myseotoolbox.crawler.spider.PageCrawledEventMatcherBuilder.aPageCrawledEvent;
import static com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory.newWebsiteCrawlFor;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlEventsIntegrationTest {


    TestCrawlJobBuilder testCrawlJobBuilder;
    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    @Autowired MonitoredUriRepository monitoredUriRepository;
    @Autowired OutboundLinkRepository outboundLinkRepository;
    @Autowired private CrawlEventDispatchFactory factory;
    @Autowired PubSubProperties pubSubProperties;
    @SpyBean MessageBrokerEventListener messageBrokerEventListener;
    @MockBean PubSubPublisherTemplate template;


    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
        testCrawlJobBuilder = new TestCrawlJobBuilder(factory);
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
        monitoredUriRepository.deleteAll();
    }

    @Test
    public void shouldNotifyOfPageCrawled() {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

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
    public void shouldNotifyOfCrawlProgress() throws InterruptedException {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(messageBrokerEventListener).onCrawlStatusUpdate(aCrawlStatusUpdateEvent().withVisited(1).build());
        verify(messageBrokerEventListener).onCrawlStatusUpdate(aCrawlStatusUpdateEvent().withVisited(2).build());
        verify(messageBrokerEventListener).onCrawlStatusUpdate(aCrawlStatusUpdateEvent().withVisited(3).build());

        //given this test is single threaded, this is deterministic. We only have 1 pending because with a single thread the crawler will go depth first
        //Bit tied with implementation details but don't think it's worth making it more generic
        verify(messageBrokerEventListener).onCrawlStatusUpdate(aCrawlStatusUpdateEvent().withPending(1).build());

        verify(messageBrokerEventListener, times(3)).onCrawlStatusUpdate(any());
    }

    @Test
    public void shouldNotifyOfWebsiteCrawlCompleted() throws InterruptedException {
        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(messageBrokerEventListener).onWebsiteCrawlCompletedEvent(new WebsiteCrawlCompletedEvent(newWebsiteCrawlFor(new ObjectId(job.getWebsiteCrawlId()), job.getCrawlOrigin().toString(), Collections.emptyList())));
    }


    @Test
    public void exceptionInOneTopicDoNotStopPublishingOtherEvents() {
        doThrow(new RuntimeException("This should not prevent update of the other"))
                .when(template).publish(eq(pubSubProperties.getPageCrawlCompletedTopicName()), any(PageCrawlCompletedEvent.class));

        givenAWebsite()
                .havingRootPage().withLinksTo("/abc", "/cde")
                .save();

        CrawlJob job = buildForSeeds(testSeeds("/"));
        job.start();

        verify(template).publish(eq(pubSubProperties.getCrawlStatusUpdateConfiguration().getTopicName()), any(CrawlStatusUpdateEvent.class));
        verify(template).publish(eq(pubSubProperties.getWebsiteCrawlCompletedTopicName()), any(WebsiteCrawlCompletedEvent.class));
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
