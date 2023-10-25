package com.myseotoolbox.crawler.spider.event;

import static org.junit.Assert.*;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksPersistenceListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.myseotoolbox.crawler.spider.event.PageCrawledEventTestBuilder.aTestPageCrawledEvent;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PageCrawledEventOutLinkPersistenceListenerTest {

    @InjectMocks
    PageCrawledEventOutLinkPersistenceListener sut; // System Under Test

    @Mock OutboundLinksPersistenceListener outLinkPersistenceListener;


    @Test
    public void onPageCrawledEventShouldPersistOutboundLink() {
        PageCrawledEvent event = aTestPageCrawledEvent().withStandardValuesForPath("/dst").build();
        sut.onPageCrawledEvent(event);
        verify(outLinkPersistenceListener).accept(event.getCrawlResult());
    }

}
