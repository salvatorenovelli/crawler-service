package com.myseotoolbox.crawler.spider.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
