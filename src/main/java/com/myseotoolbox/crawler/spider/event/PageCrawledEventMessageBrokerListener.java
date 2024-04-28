package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;


@RequiredArgsConstructor
@Slf4j
@Component
public class PageCrawledEventMessageBrokerListener {
    private final MessageBrokerEventDispatch pubSubEventDispatch;

    @EventListener
    public void onPageCrawledEvent(PageCrawledEvent event) {
        PageSnapshot snapshot = event.getCrawlResult().getPageSnapshot();
        WebsiteCrawl websiteCrawl = event.getWebsiteCrawl();
        runOrLogWarning(() -> pubSubEventDispatch.pageCrawlCompletedEvent(websiteCrawl.getId().toHexString(), snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
    }
}
