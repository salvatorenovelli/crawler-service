package com.myseotoolbox.crawler;

import com.myseotoolbox.archive.ArchiveServiceClient;
import com.myseotoolbox.crawler.model.PageCrawl;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.repository.PageCrawlRepository;
import com.myseotoolbox.crawler.testutils.CrawlHistoryTest;
import com.myseotoolbox.crawler.testutils.CrawlHistoryTestBuilder;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.myseotoolbox.crawler.StandardMetaTagValues.STANDARD_URI;
import static com.myseotoolbox.crawler.testutils.CrawlHistoryTestBuilder.a404PageSnapshot;
import static com.myseotoolbox.crawler.testutils.PageCrawlMatchers.valueType;
import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.buildRedirectChainElementsFor;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class PageCrawlPersistenceTest implements CrawlHistoryTest {


    @Mock private PageCrawlRepository repo;
    @Mock private ArchiveServiceClient archiveClient;
    public final String TEST_CRAWL_ID = new ObjectId().toHexString();

    PageCrawlPersistence sut;

    private PageSnapshot prevVal;
    private PageSnapshot curVal;

    @Before
    public void setUp() {
        sut = new PageCrawlPersistence(archiveClient, repo);
    }

    @Test
    public void canManageFirstCrawl() {

        givenCrawlHistory()
                .withCurrentValue().havingStandardValueValues()
                .build();


        sut.persistPageCrawl(TEST_CRAWL_ID, curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getRedirectChainElements(), valueType(curVal.getRedirectChainElements()));
            assertThat(crawl.getTitle(), valueType(curVal.getTitle()));
            return true;
        }));
    }

    @Test
    public void canHandle404() {
        givenCrawlHistory()
                .withCrawl().havingStandardValueValues().and()
                .withCrawl().havingStandardValueValues().and()
                .withCurrentValue(a404PageSnapshot())
                .build();

        sut.persistPageCrawl(TEST_CRAWL_ID, curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getRedirectChainElements(), valueType(buildRedirectChainElementsFor(STANDARD_URI, 404)));
            assertThat(crawl.getTitle(), valueType(""));
            return true;
        }));

    }

    @Test
    public void canHandleTemporary404() {
        givenCrawlHistory()
                .withCrawl().havingStandardValueValues().and()
                .withCrawl().havingStandardValueValues().and()
                .withCrawl().havingValue(a404PageSnapshot()).and()
                .withCurrentValue().havingStandardValueValues()
                .build();

        sut.persistPageCrawl(TEST_CRAWL_ID, curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getRedirectChainElements(), valueType(curVal.getRedirectChainElements()));
            assertThat(crawl.getTitle(), valueType(curVal.getTitle()));
            return true;
        }));

    }

    @Test
    public void shouldBeAbleToFindLastValueAutonomously() {
        givenCrawlHistory()
                .withCurrentValue().havingStandardValueValues()
                .build();

        sut.persistPageCrawl(TEST_CRAWL_ID, curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getRedirectChainElements(), valueType(curVal.getRedirectChainElements()));
            assertThat(crawl.getTitle(), valueType(curVal.getTitle()));
            return true;
        }));
    }

    @Test
    public void shouldNotPersistCanonicalizedPagesMultipleTimes() {

        PageSnapshot snapshot0 = aTestPageSnapshotForUri("http://host1/page1").build();
        PageSnapshot snapshot1 = aTestPageSnapshotForUri("http://host1/page1?t=123").withCanonicals("http://host1/page1").build();
        PageSnapshot snapshot2 = aTestPageSnapshotForUri("http://host1/page1?t=456").withCanonicals("http://host1/page1").build();

        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot0);
        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot1);
        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot2);


        verify(repo, times(1)).findTopByUriOrderByCreateDateDesc(snapshot0.getUri());

        verify(repo, times(1)).save(argThat(crawl -> {
            assertThat(crawl.getUri(), is(snapshot0.getUri()));
            return true;
        }));

        verifyNoMoreInteractions(repo);
    }

    @Test
    public void shouldNotBotherArchiveIfCanonicalized() {
        PageSnapshot snapshot0 = aTestPageSnapshotForUri("http://host1/page1").build();
        PageSnapshot snapshot1 = aTestPageSnapshotForUri("http://host1/page1?t=123").withCanonicals("http://host1/page1").build();
        PageSnapshot snapshot2 = aTestPageSnapshotForUri("http://host1/page1?t=456").withCanonicals("http://host1/page1").build();

        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot0);
        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot1);
        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot2);

        verify(archiveClient).getLastPageSnapshot(snapshot0.getUri());
        verifyNoMoreInteractions(archiveClient);
    }

    @Test
    public void shouldPersistLastCrawlInformation() {
        PageSnapshot snapshot0 = aTestPageSnapshotForUri("http://host1/page1").build();
        sut.persistPageCrawl(TEST_CRAWL_ID, snapshot0);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getLastCrawl().getWebsiteCrawlId(), is(TEST_CRAWL_ID));
            assertNotNull(crawl.getLastCrawl().getDateTime());
            assertNotNull(crawl.getLastCrawl().getInboundLinksCount());
            return true;
        }));
    }

    private CrawlHistoryTestBuilder givenCrawlHistory() {
        return new CrawlHistoryTestBuilder(this);
    }

    @Override
    public void setValues(PageCrawl prevCrawl, PageSnapshot prevVal, PageSnapshot curVal) {
        this.prevVal = prevVal;
        this.curVal = curVal;
        when(repo.findTopByUriOrderByCreateDateDesc(curVal.getUri())).thenReturn(Optional.ofNullable(prevCrawl));
        when(archiveClient.getLastPageSnapshot(anyString())).thenReturn(Optional.ofNullable(this.prevVal));
    }
}