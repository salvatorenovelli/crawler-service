package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlDelayExpired {
    private final WebsiteCrawlRepository repository;
    private final TimeUtils timeUtils;

    public boolean isCrawlDelayExpired(Workspace workspace) {
        return repository.findLatestByWorkspace(workspace.getSeqNumber())
                .map(lastCrawl -> {
                    int crawlIntervalDays = workspace.getCrawlerSettings().getCrawlIntervalDays();
                    boolean delayExpired = !timeUtils.now().minus(crawlIntervalDays, ChronoUnit.DAYS).isBefore(lastCrawl.getStartedAt());

                    if (!delayExpired) {
                        log.info("Workspace {} doesn't need crawl yet. Crawl interval: {} Last Crawl: {}",
                                workspace.getOwnerName() + " - " + workspace.getName(),
                                crawlIntervalDays,
                                lastCrawl.getStartedAt());
                    }

                    return delayExpired;
                }).orElse(true);
    }

}
