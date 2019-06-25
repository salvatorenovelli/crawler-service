package com.myseotoolbox.crawler.outboundlink;

import com.myseotoolbox.crawler.model.CrawlResult;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class OutboundLinksListenerTest {

    @Mock OutboundLinkRepository repository;
    public static final ObjectId TEST_CRAWL_ID = new ObjectId();
    private OutboundLinksListener sut;

    @Before
    public void setUp() {
        sut = new OutboundLinksListener(TEST_CRAWL_ID, repository);
    }

    @Test
    public void shouldSaveDiscoveredLinks() {
        CrawlResult crawlResult = CrawlResult.forSnapshot(
                aTestPageSnapshotForUri("http://testuri").withLinks("/relativeLink", "http://absoluteLink/hello").build());

        sut.accept(crawlResult);

        Mockito.verify(repository).save(ArgumentMatchers.argThat(argument -> {
            assertThat(argument.getCrawlId(), equalTo(TEST_CRAWL_ID));
            assertThat(argument.getUrl(), equalTo("http://testuri"));
            assertThat(argument.getLinksByType().get(LinkType.AHREF), containsInAnyOrder("/relativeLink", "http://absoluteLink/hello"));
            return true;
        }));
    }

    @Test
    public void shouldOnlyPersistSelfCanonicalized() {
        CrawlResult crawlResult = CrawlResult.forSnapshot(
                aTestPageSnapshotForUri("http://testuri?someWeirdDinamicUrl=2b234rb9b")
                        .withCanonicals("http://testuri")
                        .withLinks("/relativeLink", "http://absoluteLink/hello").build());

        sut.accept(crawlResult);

        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    public void shouldNotPersistDuplicateLinks() {
        CrawlResult crawlResult = CrawlResult.forSnapshot(
                aTestPageSnapshotForUri("http://testuri").withLinks("/relativeLink", "/relativeLink", "http://absoluteLink/hello", "http://absoluteLink/hello").build());

        sut.accept(crawlResult);

        Mockito.verify(repository).save(ArgumentMatchers.argThat(argument -> {
            assertThat(argument.getCrawlId(), equalTo(TEST_CRAWL_ID));
            assertThat(argument.getUrl(), equalTo("http://testuri"));
            assertThat(argument.getLinksByType().get(LinkType.AHREF), containsInAnyOrder("/relativeLink", "http://absoluteLink/hello"));
            return true;
        }));
    }

    @Test
    public void shouldBeFineWithNullLinks() {
        CrawlResult crawlResult = CrawlResult.forSnapshot(
                aTestPageSnapshotForUri("http://testuri").withNullLinks().build());

        sut.accept(crawlResult);

        Mockito.verify(repository).save(ArgumentMatchers.argThat(argument -> {
            assertThat(argument.getCrawlId(), equalTo(TEST_CRAWL_ID));
            assertThat(argument.getUrl(), equalTo("http://testuri"));
            assertThat(argument.getLinksByType().get(LinkType.AHREF), hasSize(0));
            return true;
        }));
    }
}