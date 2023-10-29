package com.myseotoolbox.crawler.spider.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlCompletedEventListener {
    private final MessageBrokerEventDispatch messageBrokerEventDispatch;

    @EventListener
    public void onCrawlCompleted(CrawlCompletedEvent event) {
        messageBrokerEventDispatch.onCrawlCompletedEvent(event);
    }
}
