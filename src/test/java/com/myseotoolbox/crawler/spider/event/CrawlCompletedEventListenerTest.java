package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.pagelinks.LinkType;
import com.myseotoolbox.crawler.pagelinks.OutboundLinkRepository;
import com.myseotoolbox.crawler.pagelinks.OutboundLinks;
import com.myseotoolbox.crawler.pagelinks.PageLink;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.function.Consumer;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CrawlCompletedEventListenerTest {

    @InjectMocks private CrawlCompletedEventListener sut;
    @Mock private MessageBrokerEventDispatch messageBrokerEventDispatch;

    @Test
    public void onCrawlCompletedShouldTriggerOnCrawlCompletedEvent() {
        CrawlCompletedEvent event = new CrawlCompletedEvent();
        sut.onCrawlCompleted(event);
        verify(messageBrokerEventDispatch).onCrawlCompletedEvent(event);
    }


}
