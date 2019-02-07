package com.myseotoolbox.crawl;

import com.myseotoolbox.crawl.httpclient.WebPageReader;
import com.myseotoolbox.crawl.httpclient.MonitoredUriScraper;
import com.myseotoolbox.crawl.model.MonitoredUri;
import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.model.RedirectChainElement;
import com.myseotoolbox.crawl.repository.PageSnapshotRepository;
import com.myseotoolbox.crawl.testutils.TestCalendarService;
import com.myseotoolbox.crawl.testutils.TestWebsiteBuilder;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.List;

import static com.myseotoolbox.crawl.WebPageReaderTest.TEST_ROOT_PAGE_PATH;
import static com.myseotoolbox.crawl.testutils.MonitoredUriBuilder.givenAMonitoredUri;
import static com.myseotoolbox.crawl.testutils.TestCalendarService.DEFAULT_TEST_DAY;
import static com.myseotoolbox.crawl.testutils.TestWebsiteBuilder.givenAWebsite;
import static com.myseotoolbox.crawl.testutils.TestWebsiteBuilder.testUri;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@DataMongoTest
public class MonitoredUriScraperTest {


    private WebPageReader reader = Mockito.spy(new WebPageReader());
    @Autowired private PageSnapshotRepository pageSnapshotRepository;
    @Mock private PageCrawlPersistence pageCrawlPersistence;

    MonitoredUriScraper sut;
    private CalendarService mockCalendarService = new TestCalendarService();

    @Before
    public void setUp() {

        sut = new MonitoredUriScraper(reader, pageSnapshotRepository, pageCrawlPersistence, mockCalendarService);
    }

    @After
    public void tearDown() throws Exception {
        TestWebsiteBuilder.tearDownCurrentServer();
        pageSnapshotRepository.deleteAll();
    }

    @Test
    public void nonStandardCharactersInSourceUriAreSameInSnapahost() {
        String originalUri = "http://somhost/”https://anotherhost”";
        MonitoredUri monitoredUri1 = new MonitoredUri();
        monitoredUri1.setUri(originalUri);
        sut.crawlUri(monitoredUri1);
        assertThat(pageSnapshotRepository.findAll().get(0).getUri(), is(originalUri));
    }

    @Test
    public void snapshotDateIsSavedOnSuccess() throws Exception {
        givenAWebsite()
                .havingPage(TEST_ROOT_PAGE_PATH).withTitle("Salve")
                .run();

        MonitoredUri monitoredUri = givenAMonitoredUri().forUri(testUri(TEST_ROOT_PAGE_PATH).toString()).build();

        MonitoredUri updated = sut.crawlUri(monitoredUri);


        assertThat(updated.getLastScan(), is(DEFAULT_TEST_DAY));
        assertThat(updated.getCurrentValue().getCreateDate(), is(DEFAULT_TEST_DAY));

        assertThat(pageSnapshotRepository.findAll().size(), is(1));
        assertThat(pageSnapshotRepository.findAll().get(0).getCreateDate(), is(DEFAULT_TEST_DAY));


    }

    @Test
    public void crawlDateIsSavedOnFailure() {

        MonitoredUri monitoredUri1 = new MonitoredUri();
        String malformedURI = "n/a";
        monitoredUri1.setUri(malformedURI);

        URI s = URI.create(malformedURI);
        when(reader.snapshotPage(s)).thenThrow(new RuntimeException("Malformed URL: n/a"));

        MonitoredUri updated = sut.crawlUri(monitoredUri1);

        assertThat(updated.getLastScan(), is(DEFAULT_TEST_DAY));
        assertThat(updated.getCurrentValue().getCreateDate(), is(DEFAULT_TEST_DAY));
        assertThat(pageSnapshotRepository.findAll().size(), is(1));
        assertThat(pageSnapshotRepository.findAll().get(0).getCreateDate(), is(DEFAULT_TEST_DAY));

    }

    @Test
    public void crawlMalformedUriSetErrorsOnStatus() {

        MonitoredUri monitoredUri1 = new MonitoredUri();
        String malformedURI = "n/a";
        monitoredUri1.setUri(malformedURI);

        URI s = URI.create(malformedURI);
        when(reader.snapshotPage(s)).thenThrow(new RuntimeException("Malformed URL: n/a"));

        sut.crawlUri(monitoredUri1);

        assertThat(pageSnapshotRepository.findAll().size(), is(1));
        assertThat(pageSnapshotRepository.findAll().get(0).getCrawlStatus(), containsString("Malformed URL: n/a"));
        assertThat(pageSnapshotRepository.findAll().get(0).getUri(), is(malformedURI));

    }


    @Test
    public void fullIntegrationTest() throws Exception {

        givenAWebsite()
                .havingPage(TEST_ROOT_PAGE_PATH).redirectingTo(301, "/dst").and()
                .havingPage("/dst").withTitle("You've reached the right place!")
                .run();
        MonitoredUri monitoredUri = givenAMonitoredUri().forUri(testUri(TEST_ROOT_PAGE_PATH).toString()).build();

        PageSnapshot pageSnapshot = sut.crawlUri(monitoredUri).getCurrentValue();

        assertThat(getDestinationUri(pageSnapshot), Matchers.is(testUri("/dst").toString()));
        assertThat(pageSnapshot.getTitle(), Matchers.is("You've reached the right place!"));
        assertThat(pageSnapshot.getRedirectChainElements().size(), is(2));

    }

    private String getDestinationUri(PageSnapshot pageSnapshot) {
        List<RedirectChainElement> redirectChainElements = pageSnapshot.getRedirectChainElements();
        return redirectChainElements.get(redirectChainElements.size() - 1).getDestinationURI();
    }
}