package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.repository.PageSnapshotRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.testutils.MonitoredUriBuilder;
import com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.IsolatedMongoDbTest;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.myseotoolbox.crawler.pagelinks.LinkType.SITEMAP;
import static com.myseotoolbox.crawler.testutils.MonitoredUriBuilder.TEST_WORKSPACE_NUMBER;
import static com.myseotoolbox.crawler.testutils.MonitoredUriBuilder.givenAMonitoredUri;
import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder.DEFAULT_WORKSPACE_ORIGIN;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;


public class MonitoredUriUpdaterTest extends IsolatedMongoDbTest {

    public static final String TEST_URI = DEFAULT_WORKSPACE_ORIGIN + "/path";
    @Autowired private MongoOperations operations;
    @Autowired private MonitoredUriRepository monitoredUriRepo;
    @Autowired private PageSnapshotRepository pageSnapshotRepository;
    @Autowired private WorkspaceRepository workspaceRepository;

    MonitoredUriUpdater sut;
    private static final WebsiteCrawl TEST_CRAWL = getCrawlForOrigin(DEFAULT_WORKSPACE_ORIGIN);
    private Set<URI> TEST_SITEMAP_LINKS = Set.of(
            URI.create("https://MonitoredUriUpdaterTest/sitemap.xml"), URI.create("https://MonitoredUriUpdaterTest/sitemap2.xml")
    );

    @Before
    public void setUp() {
        MonitoredUriBuilder.setUp(monitoredUriRepo, pageSnapshotRepository);
        sut = new MonitoredUriUpdater(operations, workspaceRepository);
    }

    @After
    public void tearDown() throws Exception {
        MonitoredUriBuilder.tearDown();
        workspaceRepository.deleteAll();
        monitoredUriRepo.deleteAll();
    }

    @Test
    public void shouldUpdateExistingUri() {

        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withCrawlOrigin(DEFAULT_WORKSPACE_ORIGIN).save();

        givenAMonitoredUri()
                .forUri(TEST_URI)
                .withCurrentValue().title("This is the existing title!")
                .save();

        PageSnapshot snapshot = aTestPageSnapshotForUri(TEST_URI)
                .withTitle("This is a new title")
                .build();


        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);


        List<MonitoredUri> monitoredUris = monitoredUriRepo.findByUri(TEST_URI);
        assertThat(monitoredUris, hasSize(1));

        PageSnapshot currentValue = monitoredUris.get(0).getCurrentValue();
        assertThat(currentValue.getTitle(), is("This is a new title"));
    }

    @Test
    public void shouldPersistEncodedUnicodeAsIs() {

        //At this point it should always be encoded
        givenAWorkspaceWithSeqNumber(1).save();
        PageSnapshot snapshot = aTestPageSnapshotForUri(DEFAULT_WORKSPACE_ORIGIN + "/linkWithUnicode%E2%80%8B%20%20%E2%80%8B").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAll();
        assertThat(monitoredUris, hasSize(1));

        assertThat(monitoredUris.get(0).getCurrentValue().getUri(), is("https://testhost/linkWithUnicode%E2%80%8B%20%20%E2%80%8B"));
    }

    @Test
    public void shouldUpdateMultipleMonitoredUriHavingSameUri() {

        givenAWorkspaceWithSeqNumber(1).save();
        givenAWorkspaceWithSeqNumber(2).save();


        //Multiple workspace can be monitoring same URI
        givenAMonitoredUri().forUri(TEST_URI).forWorkspace(1).withCurrentValue().title("This is the existing title1!").save();
        givenAMonitoredUri().forUri(TEST_URI).forWorkspace(2).withCurrentValue().title("This is the existing title2!").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri(TEST_URI)
                .withTitle("This is a new title")
                .build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findByUri(TEST_URI);
        assertThat(monitoredUris, hasSize(2));

        assertThat(monitoredUris.get(0).getCurrentValue().getTitle(), is("This is a new title"));
        assertThat(monitoredUris.get(1).getCurrentValue().getTitle(), is("This is a new title"));
    }

    @Test
    public void shouldCreateInCaseOfMissingMonitoredUri() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost/it-it").save();
        givenAWorkspaceWithSeqNumber(2).withCrawlOrigin("https://testhost/it-it").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/it-it/something")
                .withTitle("This is a new title")
                .build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(1));
        assertThat(monitoredUris1.get(0).getWorkspaceNumber(), is(1));
        assertThat(monitoredUris1.get(0).getCurrentValue().getTitle(), is("This is a new title"));


        List<MonitoredUri> monitoredUris2 = monitoredUriRepo.findAllByWorkspaceNumber(2);
        assertThat(monitoredUris2, hasSize(1));
        assertThat(monitoredUris2.get(0).getWorkspaceNumber(), is(2));
        assertThat(monitoredUris2.get(0).getCurrentValue().getTitle(), is("This is a new title"));
    }


    @Test
    public void shouldAddMonitoredUriEvenIfWebsiteUrlHasFilename() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost/it-it/index.html").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/it-it/something").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(1));
        assertThat(monitoredUris1.get(0).getUri(), is("https://testhost/it-it/something"));
    }

    @Test
    public void shouldFilterMonitoredUriEvenIfWebsiteUrlHasFilename() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost/it-it/index.html").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/en-gb/something").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(0));
    }

    @Test
    public void shouldNotAddMonitoredUriIfWorkspaceWebsiteUrlPathDoesNotMatch() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost/it-it/").save();
        givenAWorkspaceWithSeqNumber(2).withCrawlOrigin("https://testhost/en-gb/").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/it-it/something")
                .withTitle("This is a new title")
                .build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(1));
        assertThat(monitoredUris1.get(0).getWorkspaceNumber(), is(1));
        assertThat(monitoredUris1.get(0).getCurrentValue().getTitle(), is("This is a new title"));


        List<MonitoredUri> monitoredUris2 = monitoredUriRepo.findAllByWorkspaceNumber(2);
        assertThat(monitoredUris2, hasSize(0));
    }

    @Test
    public void trailingSlashInWebsiteOriginIsNormalized() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost/it-it").save();
        givenAWorkspaceWithSeqNumber(2).withCrawlOrigin("https://testhost/it-it/").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/it-it").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(1));

        List<MonitoredUri> monitoredUris2 = monitoredUriRepo.findAllByWorkspaceNumber(2);
        assertThat(monitoredUris2, hasSize(1));
    }

    @Test
    public void shouldPersistDifferentSchemaIfOriginSchemaMatch() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("http://testhost/page1").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(1));
    }

    @Test
    public void shouldPersistInWorkspaceOnlyIfCrawlOriginMatches() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("http://testhost/page1").build();

        sut.updateCurrentValue(getCrawlForOrigin("http://testhost"), snapshot, TEST_SITEMAP_LINKS); //different schema, it will not be the same crawl

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(0));
    }

    @Test
    public void shouldPersist_www_fromNon_www() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://www.testhost").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(1));
    }

    @Test
    public void shouldPersist_Non_www_from_www() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://www.testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(1));
    }

    @Test
    public void shouldNotCreateMonitoredUriOutsideDomain() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhostIsDifferent/page1").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);
        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(0));
    }

    @Test
    public void shouldNotIncludeSubdomains() {
        //this could be made configurable in future releases
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://subdomain.testhost").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(0));
    }

    @Test
    public void shouldSetAllTheStandardValuesInMonitoredUri() {
        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withOwner("salvatore").withCrawlOrigin("https://testhost").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/page1").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAllByWorkspaceNumber(TEST_WORKSPACE_NUMBER);
        assertThat(monitoredUris, hasSize(1));

        MonitoredUri monitoredUri = monitoredUris.get(0);
        assertThat(monitoredUri.getUri(), is(snapshot.getUri()));
        assertThat(monitoredUri.getWorkspaceNumber(), is(TEST_WORKSPACE_NUMBER));
        assertThat(monitoredUri.getLastCrawl().getWebsiteCrawlId(), is(TEST_CRAWL.getId().toHexString()));
        assertNotNull(monitoredUri.getLastCrawl().getDateTime());
    }


    @Test
    public void shouldBeAbleToHandleNullOrigin() {
        givenAWorkspaceWithSeqNumber(1).withCrawlOrigin("https://testhost/").save();
        givenAWorkspaceWithSeqNumber(2).withOwner("salvatore").withCrawlOrigin(null).save();
        givenAWorkspaceWithSeqNumber(3).withCrawlOrigin("https://testhost/").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/page1").build();
        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        assertThat(monitoredUriRepo.findAllByWorkspaceNumber(1), hasSize(1));
        assertThat(monitoredUriRepo.findAllByWorkspaceNumber(3), hasSize(1));
    }

    @Test
    public void shouldFailGracefullyWithEmptyWebsiteUrl() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/page1").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(0));
    }

    @Test
    public void shouldPersistCrawlId() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1.get(0).getLastCrawl().getWebsiteCrawlId(), is(TEST_CRAWL.getId().toHexString()));
    }

    @Test
    public void shouldResetInternalInboundLinksCount() {
        MonitoredUri build = givenAMonitoredUri()
                .forUri("https://testhost")
                .forWorkspace(0).havingInternalHrefLinksCount(10).build();
        monitoredUriRepo.save(build);

        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost").build();
        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertNull(monitoredUris1.get(0).getLastCrawl().getInboundLinksCount().getInternal());
    }

    @Test
    public void shouldNotResetExternalLinksCount() {
        MonitoredUri build = givenAMonitoredUri()
                .forUri("https://testhost")
                .forWorkspace(0).havingExternalHrefLinksCount(10).build();
        monitoredUriRepo.save(build);

        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost").build();
        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1.get(0).getLastCrawl().getInboundLinksCount().getExternal().getAhref(), is(10));
    }

    @Test
    public void shouldPersistSitemapLinks() {
        givenAWorkspaceWithSeqNumber(0).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost").build();


        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris, hasSize(1));

        Set<URI> internalLinks = monitoredUris.get(0).getLastCrawl().getInboundLinks().getInternal(SITEMAP);
        assertThat(internalLinks, is(TEST_SITEMAP_LINKS));
    }


    @Test
    public void shouldUSetStatus() {
        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://testhost/page1").withCrawlStatus("Some error").build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAllByWorkspaceNumber(TEST_WORKSPACE_NUMBER);
        assertThat(monitoredUris, hasSize(1));

        MonitoredUri monitoredUri = monitoredUris.get(0);
        assertThat(monitoredUri.getStatus(), is("Some error"));
    }

    @Test
    public void shouldUnsetStatus() {
        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withCrawlOrigin("https://testhost").save();
        PageSnapshot snapshot1 = aTestPageSnapshotForUri("https://testhost/page1").withCrawlStatus("Some error").build();
        PageSnapshot snapshot2 = aTestPageSnapshotForUri("https://testhost/page1").withCrawlStatus(null).build();

        sut.updateCurrentValue(TEST_CRAWL, snapshot1, TEST_SITEMAP_LINKS);
        sut.updateCurrentValue(TEST_CRAWL, snapshot2, TEST_SITEMAP_LINKS);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAllByWorkspaceNumber(TEST_WORKSPACE_NUMBER);
        assertThat(monitoredUris, hasSize(1));

        MonitoredUri monitoredUri = monitoredUris.get(0);
        assertNull(monitoredUri.getStatus());
    }

    private TestWorkspaceBuilder givenAWorkspaceWithSeqNumber(int seqNumber) {
        return new TestWorkspaceBuilder(workspaceRepository, seqNumber);
    }

    private static WebsiteCrawl getCrawlForOrigin(String origin) {
        return TestWebsiteCrawlFactory.newWebsiteCrawlFor(origin, emptyList());
    }
}