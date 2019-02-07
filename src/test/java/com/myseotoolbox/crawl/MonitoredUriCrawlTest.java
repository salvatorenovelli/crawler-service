package com.myseotoolbox.crawl;


import com.myseotoolbox.crawl.httpclient.WebPageReader;
import com.myseotoolbox.crawl.httpclient.MonitoredUriScraper;
import com.myseotoolbox.crawl.model.MonitoredUri;
import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.repository.MonitoredUriRepository;
import com.myseotoolbox.crawl.repository.PageSnapshotRepository;
import com.myseotoolbox.crawl.testutils.PageCrawlPreviousValueTestBuilder;
import com.myseotoolbox.crawl.testutils.TestCalendarService;
import com.myseotoolbox.crawl.testutils.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.util.Date;

import static com.myseotoolbox.crawl.testutils.TestCalendarService.DEFAULT_TEST_DAY;
import static com.myseotoolbox.crawl.testutils.TestWebsiteBuilder.givenAWebsite;
import static com.myseotoolbox.crawl.testutils.TestWebsiteBuilder.testUri;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class MonitoredUriCrawlTest {


    @Spy private WebPageReader reader = new WebPageReader();
    @Mock private PageSnapshotRepository pageSnapshotRepository;
    @Mock private MonitoredUriRepository monitoredUriRepo;
    @Mock private PageCrawlPersistence pageCrawlPersistence;
    private CalendarService calendar = new TestCalendarService();

    private MonitoredUriScraper sut;

    @Before
    public void setUp() {
        sut = new MonitoredUriScraper(reader, pageSnapshotRepository, pageCrawlPersistence, calendar);
        PageCrawlPreviousValueTestBuilder.initMocks(monitoredUriRepo, pageSnapshotRepository);
    }

    @After
    public void tearDown() throws Exception {
        TestWebsiteBuilder.tearDownCurrentServer();
    }


    @Test
    public void shouldPersistPageCrawlWithNoChanges() throws Exception {

        givenAWebsite()
                .havingRootPage().withTitle("Title 1")
                .run();

        andPreviousValueHaving()
                .title("Title 1")
                .build();

        sut.crawlUri(testWebsiteRoot());

        verify(pageCrawlPersistence).persistPageCrawl(aPageSnapshotWithTitle("Title 1"), aPageSnapshotWithTitle("Title 1"));

    }

    @Test
    public void shouldPersistPageCrawlWithChanges() throws Exception {

        givenAWebsite()
                .havingRootPage().withTitle("Title 2")
                .run();

        andPreviousValueHaving()
                .title("Title 1")
                .build();

        sut.crawlUri(testWebsiteRoot());

        verify(pageCrawlPersistence).persistPageCrawl(aPageSnapshotWithTitle("Title 1"), aPageSnapshotWithTitle("Title 2"));

    }

    @Test
    public void pageCrawlIsPersistedInTheEventOfExceptionsDuringSnapshot() throws Exception {
        givenAWebsite()
                .havingRootPage().withTitle("Title 2")
                .run();

        andPreviousValueHaving()
                .title("Title 1")
                .build();

        doThrow(new RuntimeException("This should not prevent persisting crawl")).when(reader).snapshotPage(any());

        sut.crawlUri(testWebsiteRoot());

        verify(pageCrawlPersistence).persistPageCrawl(aPageSnapshotWithTitle("Title 1"), aPageSnapshotContainingStatus("This should not prevent persisting crawl"));
    }

    @Test
    public void pageCrawlIsPersistedInTheEventOfExceptionsDuringSnapshotPersistence() throws Exception {
        givenAWebsite()
                .havingRootPage().withTitle("Title 2")
                .run();

        andPreviousValueHaving()
                .title("Title 1")
                .build();

        doThrow(new RuntimeException("This should not prevent persisting crawl")).when(reader).snapshotPage(any());

        sut.crawlUri(testWebsiteRoot());

        verify(pageCrawlPersistence).persistPageCrawl(aPageSnapshotWithTitle("Title 1"), aPageSnapshotContainingStatus("This should not prevent persisting crawl"));
    }

    @Test
    public void crawlDateIsPersisted() throws Exception {
        givenAWebsite()
                .havingRootPage().withTitle("Title 2")
                .run();

        andPreviousValueHaving()
                .title("Title 2")
                .build();

        sut.crawlUri(testWebsiteRoot());

        verify(pageCrawlPersistence).persistPageCrawl(any(), aPageSnapshotWithDate(DEFAULT_TEST_DAY));
    }

    private PageSnapshot aPageSnapshotWithDate(Date expected) {
        return argThat(argument -> argument.getCreateDate().equals(expected));
    }

    private PageSnapshot aPageSnapshotContainingStatus(String status) {
        return argThat(argument -> argument.getCrawlStatus().contains(status));
    }


    private PageSnapshot aPageSnapshotWithTitle(String title) {
        return argThat(argument -> title.equals(argument.getTitle()));
    }

    private PageCrawlPreviousValueTestBuilder andPreviousValueHaving() throws URISyntaxException {
        return new PageCrawlPreviousValueTestBuilder(testUri("/").toString());
    }

    private MonitoredUri testWebsiteRoot() throws URISyntaxException {
        return monitoredUriRepo.findByUri(testUri("/").toString()).get();
    }
}
