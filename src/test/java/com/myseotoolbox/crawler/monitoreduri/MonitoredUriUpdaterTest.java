package com.myseotoolbox.crawler.monitoreduri;

import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.repository.PageSnapshotRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.testutils.MonitoredUriBuilder;
import com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.myseotoolbox.crawler.testutils.MonitoredUriBuilder.givenAMonitoredUri;
import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static com.myseotoolbox.crawler.testutils.TestWorkspaceBuilder.DEFAULT_TEST_WEBSITE_URL;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@DataMongoTest
public class MonitoredUriUpdaterTest {

    public static final String TEST_URI = DEFAULT_TEST_WEBSITE_URL + "/path";
    public static final int TEST_WORKSPACE_NUMBER = 23;
    @Autowired private MongoOperations operations;
    @Autowired private MonitoredUriRepository monitoredUriRepo;
    @Autowired private PageSnapshotRepository pageSnapshotRepository;
    @Autowired private WorkspaceRepository workspaceRepository;

    MonitoredUriUpdater sut;

    @Before
    public void setUp() {
        MonitoredUriBuilder.setUp(monitoredUriRepo, pageSnapshotRepository);
        sut = new MonitoredUriUpdater(operations, workspaceRepository);
    }

    @After
    public void tearDown() throws Exception {
        MonitoredUriBuilder.tearDown();
        workspaceRepository.deleteAll();
    }

    @Test
    public void shouldUpdateExistingUri() {

        givenAWorkspaceWithSeqNumber(MonitoredUriBuilder.TEST_WORKSPACE_NUMBER).withWebsiteUrl("http://host/").save();

        givenAMonitoredUri()
                .forUri(TEST_URI)
                .withCurrentValue().title("This is the existing title!")
                .save();

        PageSnapshot snapshot = aTestPageSnapshotForUri(TEST_URI)
                .withTitle("This is a new title")
                .build();


        sut.updateCurrentValue(snapshot);


        List<MonitoredUri> monitoredUris = monitoredUriRepo.findByUri(TEST_URI);
        assertThat(monitoredUris, hasSize(1));

        PageSnapshot currentValue = monitoredUris.get(0).getCurrentValue();
        assertThat(currentValue.getTitle(), is("This is a new title"));
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

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findByUri(TEST_URI);
        assertThat(monitoredUris, hasSize(2));

        assertThat(monitoredUris.get(0).getCurrentValue().getTitle(), is("This is a new title"));
        assertThat(monitoredUris.get(1).getCurrentValue().getTitle(), is("This is a new title"));
    }

    @Test
    public void shouldCreateInCaseOfMissingMonitoredUri() {
        givenAWorkspaceWithSeqNumber(1).withWebsiteUrl("http://host/it-it").save();
        givenAWorkspaceWithSeqNumber(2).withWebsiteUrl("http://host/it-it").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host/it-it/something")
                .withTitle("This is a new title")
                .build();

        sut.updateCurrentValue(snapshot);


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
    public void shouldNotAddMonitoredUriIfWorkspaceWebsiteUrlDoesNotMatch() {
        givenAWorkspaceWithSeqNumber(1).withWebsiteUrl("http://host/it-it").save();
        givenAWorkspaceWithSeqNumber(2).withWebsiteUrl("http://host/en-gb").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host/it-it/something")
                .withTitle("This is a new title")
                .build();

        sut.updateCurrentValue(snapshot);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(1));
        assertThat(monitoredUris1.get(0).getWorkspaceNumber(), is(1));
        assertThat(monitoredUris1.get(0).getCurrentValue().getTitle(), is("This is a new title"));


        List<MonitoredUri> monitoredUris2 = monitoredUriRepo.findAllByWorkspaceNumber(2);
        assertThat(monitoredUris2, hasSize(0));
    }

    @Test
    public void trailingSlashInWebsiteOriginIsNormalized() {
        givenAWorkspaceWithSeqNumber(1).withWebsiteUrl("http://host/it-it").save();
        givenAWorkspaceWithSeqNumber(2).withWebsiteUrl("http://host/it-it/").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host/it-it").build();

        sut.updateCurrentValue(snapshot);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(1));

        List<MonitoredUri> monitoredUris2 = monitoredUriRepo.findAllByWorkspaceNumber(2);
        assertThat(monitoredUris2, hasSize(1));
    }

    @Test
    public void shouldPersistDifferentSchema() {
        givenAWorkspaceWithSeqNumber(0).withWebsiteUrl("https://host").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host/page1").build();

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(1));
    }

    @Test
    public void shouldPersist_www_fromNon_www() {
        givenAWorkspaceWithSeqNumber(0).withWebsiteUrl("https://host").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://www.host").build();

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(1));
    }

    @Test
    public void shouldPersist_Non_www_from_www() {
        givenAWorkspaceWithSeqNumber(0).withWebsiteUrl("https://www.host").save();
        PageSnapshot snapshot = aTestPageSnapshotForUri("https://host").build();

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(1));
    }

    @Test
    public void shouldNotCreateMonitoredUriOutsideDomain() {
        givenAWorkspaceWithSeqNumber(0).withWebsiteUrl("https://host").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://hostIsDifferent/page1").build();

        sut.updateCurrentValue(snapshot);
        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(0));

    }

    @Test
    public void shouldNotIncludeSubdomains() {
        //this could be made configurable in future releases
        givenAWorkspaceWithSeqNumber(1).withWebsiteUrl("http://host").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://subdomain.host").build();

        sut.updateCurrentValue(snapshot);


        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(1);
        assertThat(monitoredUris1, hasSize(0));

    }

    @Test
    public void shouldSetAllTheStandardValuesInMonitoredUri() {
        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withOwner("salvatore").withWebsiteUrl("http://host").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host/page1").build();

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAllByWorkspaceNumber(TEST_WORKSPACE_NUMBER);
        assertThat(monitoredUris, hasSize(1));

        MonitoredUri monitoredUri = monitoredUris.get(0);
        assertThat(monitoredUri.getUri(), is(snapshot.getUri()));
        assertThat(monitoredUri.getWorkspaceNumber(), is(TEST_WORKSPACE_NUMBER));
        assertNotNull(monitoredUri.getLastScan());

    }

    @Test
    public void shouldSanitizeTags() {

        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withWebsiteUrl("http://host/").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host/page1")
                .withTitle("This title contains dirty &nbsp; characters")
                .build();

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris = monitoredUriRepo.findAllByWorkspaceNumber(TEST_WORKSPACE_NUMBER);
        MonitoredUri monitoredUri = monitoredUris.get(0);
        assertThat(monitoredUri.getCurrentValue().getTitle(), is("This title contains dirty characters"));

    }


    @Test
    public void itShouldNotPersistCanonicalizedPagesTwice() {

        givenAWorkspaceWithSeqNumber(TEST_WORKSPACE_NUMBER).withWebsiteUrl("http://host1/").save();

        PageSnapshot snapshot0 = aTestPageSnapshotForUri("http://host1/page1").build();
        PageSnapshot snapshot1 = aTestPageSnapshotForUri("http://host1/page1?t=123").withCanonicals("http://host1/page1").build();
        PageSnapshot snapshot2 = aTestPageSnapshotForUri("http://host1/page1?t=456").withCanonicals("http://host1/page1").build();

        sut.updateCurrentValue(snapshot0);
        sut.updateCurrentValue(snapshot1);
        sut.updateCurrentValue(snapshot2);

        assertThat(monitoredUriRepo.findAll(), hasSize(1));
        assertThat(monitoredUriRepo.findAll().get(0).getUri(), is("http://host1/page1"));

    }

    @Test
    public void shouldBeAbleToHandleNullOrigin() {
        givenAWorkspaceWithSeqNumber(1).withWebsiteUrl("http://host1/").save();
        givenAWorkspaceWithSeqNumber(2).withOwner("salvatore").withWebsiteUrl(null).save();
        givenAWorkspaceWithSeqNumber(3).withWebsiteUrl("http://host1/").save();

        PageSnapshot snapshot0 = aTestPageSnapshotForUri("http://host1/page1").build();
        sut.updateCurrentValue(snapshot0);

        assertThat(monitoredUriRepo.findAllByWorkspaceNumber(1), hasSize(1));
        assertThat(monitoredUriRepo.findAllByWorkspaceNumber(3), hasSize(1));
    }

    @Test
    public void shouldFailGracefullyWithEmptyWebsiteUrl() {
        givenAWorkspaceWithSeqNumber(0).withWebsiteUrl("").save();

        PageSnapshot snapshot = aTestPageSnapshotForUri("http://host1/page1").build();

        sut.updateCurrentValue(snapshot);

        List<MonitoredUri> monitoredUris1 = monitoredUriRepo.findAllByWorkspaceNumber(0);
        assertThat(monitoredUris1, hasSize(0));
    }

    private TestWorkspaceBuilder givenAWorkspaceWithSeqNumber(int seqNumber) {
        return new TestWorkspaceBuilder(workspaceRepository, seqNumber);
    }

}