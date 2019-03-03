package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.model.PageCrawl;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.ResolvableField;
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

import static com.myseotoolbox.crawler.StandardMetaTagValues.STANDARD_DATE;
import static com.myseotoolbox.crawler.StandardMetaTagValues.STANDARD_URI;
import static com.myseotoolbox.crawler.testutils.CrawlHistoryTestBuilder.a404PageSnapshot;
import static com.myseotoolbox.crawler.testutils.PageCrawlMatchers.referenceTo;
import static com.myseotoolbox.crawler.testutils.PageCrawlMatchers.valueType;
import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.buildRedirectChainElementsFor;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class PageCrawlPersistenceTest implements CrawlHistoryTest {


    @Mock private PageCrawlRepository repo;
    @Mock private ArchiveServiceClient archiveClient;

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


        sut.persistPageCrawl(curVal);

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

        sut.persistPageCrawl(curVal);

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

        sut.persistPageCrawl(curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getRedirectChainElements(), valueType(curVal.getRedirectChainElements()));
            assertThat(crawl.getTitle(), valueType(curVal.getTitle()));
            return true;
        }));

    }

    @Test
    public void sanitizedDifferencesAreIgnored() {

        //Prev values are sanitized before being persisted PageSnapshotSanitizer.
        //If we don't sanitize current value we are going  to see differences in values that are not really persisted, AND ignored on the frontend

        givenCrawlHistory()
                .withCrawl().havingStandardValueValues().withTitle("Multiple spaces is sanitized").and()
                .withCurrentValue().havingStandardValueValues().withTitle("Multiple      spaces is \nsanitized")
                .build();

        sut.persistPageCrawl(curVal);


        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getTitle(), referenceTo(ResolvableField.forReference(new ObjectId(STANDARD_DATE, 0))));
            return true;
        }));

    }

    @Test
    public void sanitizedDifferencesAreIgnoredInLists() {

        //Prev values are sanitized before being persisted PageSnapshotSanitizer.
        //If we don't sanitize current value we are going  to see differences in values that are not really persisted, AND ignored on the frontend

        givenCrawlHistory()
                .withCrawl().havingStandardValueValues().withH1s("HTML character like '<' are sanitized").and()
                .withCurrentValue().havingStandardValueValues().withH1s("HTML character like '&lt;' are sanitized")
                .build();

        sut.persistPageCrawl(curVal);


        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getH1s(), referenceTo(ResolvableField.forReference(new ObjectId(STANDARD_DATE, 0))));
            return true;
        }));

    }

    @Test
    public void itShouldPersistSanitizedTags() {
        givenCrawlHistory()
                .withCurrentValue().havingStandardValueValues().withH1s("HTML character like '&lt;' are sanitized")
                .build();

        sut.persistPageCrawl(curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getH1s(), valueType(singletonList("HTML character like '<' are sanitized")));
            return true;
        }));

    }

    @Test
    public void shouldBeAbleToFindLastValueAutonomously() {
        givenCrawlHistory()
                .withCurrentValue().havingStandardValueValues()
                .build();

        sut.persistPageCrawl(curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getRedirectChainElements(), valueType(curVal.getRedirectChainElements()));
            assertThat(crawl.getTitle(), valueType(curVal.getTitle()));
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