package com.myseotoolbox.crawler.outboundlink;

import com.myseotoolbox.crawler.model.CrawlResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class OutboundLinksListenerTest {

    @Mock OutboundLinkRepository repository;


    @Test
    public void shouldSaveDiscoveredLinks() {
        String crawlId = "32o4572348cf";
        OutboundLinksListener sut = new OutboundLinksListener(crawlId, repository);

        CrawlResult crawlResult = CrawlResult.forSnapshot(
                aTestPageSnapshotForUri("http://testuri").withLinks("/relativeLink", "http://absoluteLink/hello").build());

        sut.accept(crawlResult);

        Mockito.verify(repository).save(ArgumentMatchers.argThat(argument -> {
            assertThat(argument.getCrawlId(), equalTo(crawlId));
            assertThat(argument.getUrl(), equalTo("http://testuri"));
            assertThat(argument.getLinks(), containsInAnyOrder(ahref("/relativeLink"), ahref("http://absoluteLink/hello")));
            return true;
        }));
    }

    private Link ahref(String uri) {
        return new Link(Link.Type.AHREF, uri);
    }
}