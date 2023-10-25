package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.spider.PubSubEventDispatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.myseotoolbox.crawler.spider.event.PageCrawledEventTestBuilder.aTestPageCrawledEvent;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class PageCrawledEventPubSubListenerTest {

    @InjectMocks PageCrawledEventPubSubListener sut;
    @Mock private PubSubEventDispatch pubSubEventDispatch;

    @Test
    public void onPageCrawledEventShouldPersistOutboundLink() {
        PageCrawledEvent event = aTestPageCrawledEvent().withStandardValuesForPath("/dst").build();
        sut.onPageCrawledEvent(event);
        verify(pubSubEventDispatch).pageCrawlCompletedEvent(event.getWebsiteCrawl().getId().toHexString(), event.getCrawlResult().getPageSnapshot());
    }
}