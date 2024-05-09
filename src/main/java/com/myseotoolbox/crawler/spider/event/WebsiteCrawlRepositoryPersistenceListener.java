package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsiteCrawlRepositoryPersistenceListener {

    private final WebsiteCrawlRepository websiteCrawlRepository;

    @EventListener
    public void persistOnRepository(WebsiteCrawlStartedEvent event) {
        WebsiteCrawl websiteCrawl = event.getWebsiteCrawl();
        log.debug("Persisting CrawlStartedEvent: {}", websiteCrawl);
        runOrLogWarning(() -> websiteCrawlRepository.save(websiteCrawl), "Error while persisting CrawlStartedEvent: " + websiteCrawl);
    }
}