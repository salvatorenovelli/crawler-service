package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.pagelinks.OutboundLinksPersistenceListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageCrawledEventOutLinkPersistenceListener {

    private final OutboundLinksPersistenceListener outLinkPersistenceListener;

    @EventListener
    public void onPageCrawledEvent(PageCrawledEvent event) {
        CrawlResult crawlResult = event.getCrawlResult();
        runOrLogWarning(() -> outLinkPersistenceListener.accept(crawlResult), "Error while persisting outbound links for uri: " + crawlResult.getUri());
    }


}