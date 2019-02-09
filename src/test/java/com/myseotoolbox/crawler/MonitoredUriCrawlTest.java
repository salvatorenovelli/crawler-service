package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.httpclient.MonitoredUriScraper;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.MonitoredUri;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.MonitoredUriRepository;
import com.myseotoolbox.crawler.repository.PageSnapshotRepository;
import com.myseotoolbox.crawler.testutils.PageCrawlPreviousValueTestBuilder;
import com.myseotoolbox.crawler.testutils.TestCalendarService;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import static com.myseotoolbox.crawler.testutils.TestCalendarService.DEFAULT_TEST_DAY;
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
    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    @Before
    public void setUp() {
        sut = new MonitoredUriScraper(reader, pageSnapshotRepository, pageCrawlPersistence, calendar);
        PageCrawlPreviousValueTestBuilder.initMocks(monitoredUriRepo, pageSnapshotRepository);
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.stop();
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

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
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

    private URI testUri(String s) throws URISyntaxException {
        return testWebsiteBuilder.buildTestUri(s);
    }

    private MonitoredUri testWebsiteRoot() throws URISyntaxException {
        return monitoredUriRepo.findByUri(testUri("/").toString()).get();
    }
}
