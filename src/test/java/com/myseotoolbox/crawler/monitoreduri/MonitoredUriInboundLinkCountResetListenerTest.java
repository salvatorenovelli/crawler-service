package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.event.WebsiteCrawlCompletedEvent;
import com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder;
import com.myseotoolbox.crawler.websitecrawl.CrawlTrigger;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import com.myseotoolbox.testutils.IsolatedMongoDbTest;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import java.time.Instant;
import java.util.List;

import static com.myseotoolbox.crawler.testutils.MonitoredUriBuilder.givenAMonitoredUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MonitoredUriInboundLinkCountResetListenerTest extends IsolatedMongoDbTest {

    private static final int TEST_WORKSPACE_NUMBER = 99999;
    private static final int OTHER_WORKSPACE_NUMBER = 88888;
    private static final String OLD_CRAWL_ID = new ObjectId().toHexString();

    private static final WebsiteCrawl TEST_WEBSITE_CRAWL = WebsiteCrawlFactory.newWebsiteCrawlFor("testOwner",
            new CrawlTrigger(CrawlTrigger.Type.USER_INITIATED_WORKSPACE, List.of(TEST_WORKSPACE_NUMBER)), "http://origin", List.of());

    @Autowired private MonitoredUriRepository repository;
    @Autowired private MongoOperations operations;
    @Autowired private WorkspaceRepository workspaceRepository;
    private MonitoredUriInboundLinkCountResetListener sut;

    @Before
    public void setUp() {
        sut = new MonitoredUriInboundLinkCountResetListener(operations);
        repository.deleteAll();
        givenAWorkspace(TEST_WORKSPACE_NUMBER).save();
        givenAWorkspace(OTHER_WORKSPACE_NUMBER).save();
    }

    @Test
    public void shouldOnlyResetIfNotLastCrawl() {

        MonitoredUri uriWithOldCrawl = repository.save(
                givenAMonitoredUri()
                        .forWorkspace(TEST_WORKSPACE_NUMBER)
                        .havingLastCrawl(OLD_CRAWL_ID, 100)
                        .build()
        );

        MonitoredUri uriWithNewCrawl = repository.save(
                givenAMonitoredUri()
                        .forWorkspace(TEST_WORKSPACE_NUMBER)
                        .havingLastCrawl(TEST_WEBSITE_CRAWL.getId().toHexString(), 200)
                        .build()
        );

        WebsiteCrawlCompletedEvent event = new WebsiteCrawlCompletedEvent(TEST_WEBSITE_CRAWL, 1, Instant.EPOCH);

        sut.onWebsiteCrawlCompletedEvent(event);

        MonitoredUri updatedOldCrawlUri = repository.findById(uriWithOldCrawl.getId()).orElseThrow();
        MonitoredUri updatedNewCrawlUri = repository.findById(uriWithNewCrawl.getId()).orElseThrow();

        assertThat(updatedOldCrawlUri.getLastCrawl().getInboundLinksCount().getInternal().getAhref(), is(0));
        assertThat(updatedNewCrawlUri.getLastCrawl().getInboundLinksCount().getInternal().getAhref(), is(200));
    }

    @Test
    public void shouldNotResetInboundLinksForOtherWorkspaces() {
        MonitoredUri uriInTargetWorkspace = repository.save(
                givenAMonitoredUri()
                        .forWorkspace(TEST_WORKSPACE_NUMBER)
                        .havingLastCrawl(OLD_CRAWL_ID, 100)
                        .build()
        );

        MonitoredUri uriInOtherWorkspace = repository.save(
                givenAMonitoredUri()
                        .forWorkspace(OTHER_WORKSPACE_NUMBER)
                        .havingLastCrawl(OLD_CRAWL_ID, 300)
                        .build()
        );

        WebsiteCrawlCompletedEvent event = new WebsiteCrawlCompletedEvent(TEST_WEBSITE_CRAWL, 1, Instant.EPOCH);

        sut.onWebsiteCrawlCompletedEvent(event);

        MonitoredUri updatedUriInTargetWorkspace = repository.findById(uriInTargetWorkspace.getId()).orElseThrow();
        MonitoredUri updatedUriInOtherWorkspace = repository.findById(uriInOtherWorkspace.getId()).orElseThrow();

        assertThat(updatedUriInTargetWorkspace.getLastCrawl().getInboundLinksCount().getInternal().getAhref(), is(0));
        assertThat(updatedUriInOtherWorkspace.getLastCrawl().getInboundLinksCount().getInternal().getAhref(), is(300));
    }

    @Test
    public void itShouldHandleNulls() {

        sut.onWebsiteCrawlCompletedEvent(
                new WebsiteCrawlCompletedEvent(
                        WebsiteCrawlFactory.newWebsiteCrawlFor("testOwner", null, "http://origin", List.of()),
                        1, Instant.EPOCH
                )
        );

        sut.onWebsiteCrawlCompletedEvent(
                new WebsiteCrawlCompletedEvent(
                        WebsiteCrawlFactory.newWebsiteCrawlFor("testOwner",
                                new CrawlTrigger(CrawlTrigger.Type.USER_INITIATED_WORKSPACE, null),
                                "http://origin", List.of()
                        ),
                        1, Instant.EPOCH
                )
        );
    }

    private TestWorkspaceBuilder givenAWorkspace(int workspaceNumber) {
        return new TestWorkspaceBuilder(workspaceRepository, workspaceNumber);
    }
}
