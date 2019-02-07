package com.myseotoolbox.crawl;

import com.myseotoolbox.crawl.model.PageCrawl;
import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.model.ResolvableField;
import com.myseotoolbox.crawl.repository.PageCrawlRepository;
import com.myseotoolbox.crawl.testutils.CrawlHistoryTest;
import com.myseotoolbox.crawl.testutils.CrawlHistoryTestBuilder;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.myseotoolbox.crawl.StandardMetaTagValues.STANDARD_DATE;
import static com.myseotoolbox.crawl.StandardMetaTagValues.STANDARD_URI;
import static com.myseotoolbox.crawl.testutils.CrawlHistoryTestBuilder.a404PageSnapshot;
import static com.myseotoolbox.crawl.testutils.PageCrawlMatchers.referenceTo;
import static com.myseotoolbox.crawl.testutils.PageCrawlMatchers.valueType;
import static com.myseotoolbox.crawl.testutils.PageSnapshotTestBuilder.buildRedirectChainElementsFor;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PageCrawlPersistenceTest implements CrawlHistoryTest {


    @Mock private PageCrawlRepository repo;
    PageCrawlPersistence sut;

    private PageSnapshot prevVal;
    private PageSnapshot curVal;

    @Before
    public void setUp() throws Exception {
        sut = new PageCrawlPersistence(repo);
    }

    @Test
    public void canManageFirstCrawl() {

        givenCrawlHistory()
                .withCurrentValue().havingStandardValueValues()
                .build();


        sut.persistPageCrawl(prevVal, curVal);

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

        sut.persistPageCrawl(prevVal, curVal);

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

        sut.persistPageCrawl(prevVal, curVal);

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

        sut.persistPageCrawl(prevVal, curVal);


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

        sut.persistPageCrawl(prevVal, curVal);


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


        sut.persistPageCrawl(prevVal, curVal);

        verify(repo).save(argThat(crawl -> {
            assertThat(crawl.getH1s(),valueType(singletonList("HTML character like '<' are sanitized")));
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
    }
}