package com.myseotoolbox.crawl;

import com.myseotoolbox.crawl.httpclient.WebPageReader;
import com.myseotoolbox.crawl.httpclient.MonitoredUriScraper;
import com.myseotoolbox.crawl.model.EntityNotFoundException;
import com.myseotoolbox.crawl.model.MonitoredUri;
import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.repository.MonitoredUriRepository;
import com.myseotoolbox.crawl.repository.PageSnapshotRepository;
import com.myseotoolbox.crawl.repository.WorkspaceRepository;
import com.myseotoolbox.crawl.testutils.MonitoredUriBuilder;
import com.myseotoolbox.crawl.testutils.WorkspaceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.myseotoolbox.crawl.testutils.DataTestHelper.clearRepos;
import static com.myseotoolbox.crawl.testutils.MonitoredUriBuilder.TEST_WORKSPACE_NUMBER;
import static com.myseotoolbox.crawl.testutils.MonitoredUriBuilder.givenAMonitoredUri;
import static com.myseotoolbox.crawl.testutils.PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
@DataMongoTest
public class MonitoredUriScannerTest {


    private static final String USER_1 = "user1@test";
    private static final String USER_2 = "user2@test";
    private static final String URI_1 = "http://uri1";
    private static final LocalDate JUST_NOW = LocalDate.now();
    private static final LocalDate YESTERDAY = JUST_NOW.minusDays(1);
    @Autowired private PageSnapshotRepository pageSnapshotRepo;
    @Autowired private MonitoredUriRepository monitoredUriRepo;
    @Autowired private WorkspaceRepository workspaceRepository;
    private CalendarService calendar = new CalendarService();


    MonitoredUriScraper crawler;
    @Mock private WebPageReader reader;
    @Mock private PageCrawlPersistence pageCrawlPersistence;

    private MonitoredUriScanner sut;
    private WorkspaceBuilder builder;


    @Before
    public void setUp() {
        crawler = new MonitoredUriScraper(reader, pageSnapshotRepo, pageCrawlPersistence, calendar);

        when(reader.snapshotPage(any())).then(invocation -> aPageSnapshotWithStandardValuesForUri(invocation.getArguments()[0].toString()));

        MonitoredUriBuilder.setUp(monitoredUriRepo, pageSnapshotRepo);
        sut = new MonitoredUriScanner(crawler, monitoredUriRepo, workspaceRepository, Runnable::run);

        builder = new WorkspaceBuilder(workspaceRepository);
        builder.givenWorkspace(TEST_WORKSPACE_NUMBER).build();

    }

    @After
    public void tearDown() {
        MonitoredUriBuilder.tearDown();
        clearRepos(workspaceRepository);
    }



    @Test
    public void scanSingleUri() throws EntityNotFoundException {
        givenAMonitoredUri().forUri("http://uri1").forUser(USER_1).save();
        givenAMonitoredUri().forUri("http://uri2").forUser(USER_1).save();
        givenAMonitoredUri().forUri("http://uri3").forUser(USER_2).save();

        sut.scanSingleUri("http://uri1");

        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri1")));
        verifyNoMoreInteractions(reader);
    }

    @Test
    public void itShouldCrawlAllTheUriFromAllTheUsers() throws IOException {

        givenAMonitoredUri().forUri("http://uri1").forUser(USER_1).save();
        givenAMonitoredUri().forUri("http://uri2").forUser(USER_1).save();
        givenAMonitoredUri().forUri("http://uri3").forUser(USER_2).save();

        sut.scanAll();

        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri1")));
        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri2")));
        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri3")));

    }

    @Test
    public void scanAllShouldNotScanTwiceIfPaginationOccur() {

        int nMonitoredUri = MonitoredUriScanner.DEFAULT_PAGE_SIZE * 3;

        List<MonitoredUri> savedUri = IntStream.range(0, nMonitoredUri)
                .mapToObj(operand -> givenAMonitoredUri()
                        .forUser(USER_1)
                        .save())
                .collect(Collectors.toList());

        sut.scanAll();

        savedUri.forEach(monitoredUri -> {
            verify(reader, times(1)).snapshotPage(URI.create(monitoredUri.getUri()));
        });

        verifyNoMoreInteractions(reader);
    }

    @Test
    public void scanWorkspaceShouldNotScanTwiceIfPaginationOccur() {

        int nMonitoredUri = MonitoredUriScanner.DEFAULT_PAGE_SIZE * 3;

        List<MonitoredUri> savedUri = IntStream.range(0, nMonitoredUri)
                .mapToObj(operand -> givenAMonitoredUri()
                        .forUser(USER_1)
                        .save())
                .collect(Collectors.toList());

        sut.asyncScanWorkspace(TEST_WORKSPACE_NUMBER);

        savedUri.forEach(monitoredUri -> {
            verify(reader, times(1)).snapshotPage(URI.create(monitoredUri.getUri()));
        });

        verifyNoMoreInteractions(reader);
    }

    @Test
    public void shouldOnlyCrawlSelectedWorkspace() throws IOException {

        givenAMonitoredUri().forWorkspace(TEST_WORKSPACE_NUMBER).forUri("http://uri1").save();
        givenAMonitoredUri().forWorkspace(TEST_WORKSPACE_NUMBER).forUri("http://uri2").save();
        givenAMonitoredUri().forWorkspace(TEST_WORKSPACE_NUMBER + 1).forUri("http://uri3").save();

        sut.asyncScanWorkspace(TEST_WORKSPACE_NUMBER);

        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri1")));
        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri2")));
        verify(reader, times(0)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri3")));

    }

    @Test
    public void itShouldSaveTheUriForTheCrawledUri() {

        givenAMonitoredUri().forUri(URI_1).forUser(USER_1).save();

        sut.scanAll();

        assertThat(pageSnapshotRepo.findAll(), hasSize(1));
        assertThat(pageSnapshotRepo.findAll().get(0).getUri(), is(URI_1));

    }


    @Test
    public void shouldScanAllWorkspaces() {

        builder.givenWorkspace(1234).and()
                .givenWorkspace(5678).build();

        givenAMonitoredUri().forUri("http://uri1234").forWorkspace(1234).save();
        givenAMonitoredUri().forUri("http://uri5678").forWorkspace(5678).save();

        sut.scanAll();

        verify(reader, times(1)).snapshotPage(argThat(uri -> uri.toString().equals("http://uri1234")));
        verify(reader, times(1)).snapshotPage(argThat(uri -> uri.toString().equals("http://uri5678")));
    }


    @Test
    public void currentValueIsSavedInCaseOfException() {
        givenAMonitoredUri().forUri("not a valid uri1").forUser(USER_1).save();

        sut.scanAll();

        MonitoredUri crawled = monitoredUriRepo.findAll().get(0);
        PageSnapshot currentValue = crawled.getCurrentValue();

        assertThat(currentValue.getUri(), is("not a valid uri1"));
        assertNotNull(currentValue.getCreateDate());
        assertThat(currentValue.getCrawlStatus(), containsString("not a valid uri1"));
        assertThat(currentValue.getCrawlStatus(), containsString("Illegal character in path"));
    }

    @Test
    public void exceptionHappenedDuringSnapshotAreSavedToCrawlStatus() {
        givenAMonitoredUri().forUri("not a valid uri1").forUser(USER_1).save();

        sut.scanAll();

        MonitoredUri crawled = monitoredUriRepo.findAll().get(0);
        assertThat(crawled.getCurrentValue().getCrawlStatus(), containsString("not a valid uri1"));
    }

    @Test
    public void itShouldHandleExceptionsWhileSaving() {
        givenAMonitoredUri().forUri("not a valid uri1").forUser(USER_1).save();

        sut.scanAll();

        MonitoredUri crawled = monitoredUriRepo.findAll().get(0);
        assertThat(crawled.getUri(), is("not a valid uri1"));
    }


    @Test
    public void exceptionsDuringCrawlDontInterruptTheScan() {
        givenAMonitoredUri().forUri("not a valid uri1").forUser(USER_1).save();
        givenAMonitoredUri().forUri("http://valid_one").forUser(USER_1).save();

        sut.scanAll();

        assertThat(monitoredUriRepo.findAll().size(), is(2));
    }

    @Test
    public void exceptionsDuringSaveDontInterruptTheScan() {
        MonitoredUriRepository mockMonitoredUriRepository = initRepoToThrowExceptionOnSave();
        sut = new MonitoredUriScanner(crawler, mockMonitoredUriRepository, workspaceRepository, Runnable::run);

        sut.scanAll();

        verify(mockMonitoredUriRepository, times(3)).save(any());
    }


    @Test
    @Ignore
    public void ifTwoUserHaveSameMonitoredUri_scanIsNotExecutedButCurrentValueShouldBeUpdated() {
        //but all the currentvalue should be updated (or at least until current value is still a thing)
        fail("not implemented");
    }

    @Test
    @Ignore
    public void multipleSnapshotPerDayOnSameUriRateLimiting() {

        /*
           Problems:

           multiple users can request more snapshot on same uri
             - Database can be bloated (api rate limit should prevent disaster but still 30 snapshot in the same day seem useless)
             - overriding the old snapshot can prevent db bloat but remove information.
             Maybe we can store the additional snapshots per user and limit that

         */


        fail("Not implemented");
    }

    @Test
    @Ignore
    public void itShouldNotScanTwiceIfTwoUsersHaveTheSameMonitoredUri() throws IOException {

        givenAMonitoredUri()
                .forUser(USER_1)
                .forUri("http://uri1")
                .withCurrentValue().scanned(JUST_NOW).and()
                .save();

        givenAMonitoredUri()
                .forUser(USER_2)
                .forUri("http://uri1")
                .withCurrentValue().scanned(YESTERDAY).and()
                .save();


        sut.scanAll();

        verify(reader, times(1)).snapshotPage(argThat(argument -> argument.toString().equals("http://uri1")));
        verifyNoMoreInteractions(reader);

    }

    /**
     * Returns three {@link MonitoredUri} in findAllByUserName but throws exception when the second is saved
     */
    private MonitoredUriRepository initRepoToThrowExceptionOnSave() {
        MonitoredUriRepository repository = Mockito.mock(MonitoredUriRepository.class);

        when(repository.findAllByWorkspaceNumber(eq(TEST_WORKSPACE_NUMBER), any())).thenReturn(new PageImpl<>(Arrays.asList(
                monitoredUriForUri("http://valid1"),
                monitoredUriForUri("http://valid2"),
                monitoredUriForUri("http://valid3"))));

        when(repository.countByWorkspaceNumber(TEST_WORKSPACE_NUMBER)).thenReturn(3L);

        when(repository.save(argThat(t -> t.getUri().equals("http://valid2"))))
                .thenThrow(new RuntimeException("This exception should not prevent the scan"));

        return repository;
    }

    private MonitoredUri monitoredUriForUri(String uri) {
        MonitoredUri it = new MonitoredUri();
        it.setUri(uri);
        return it;
    }

}