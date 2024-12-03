package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.spider.BulkWorkspaceCrawlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Profile("!dev")
@Slf4j
public class ScanScheduler {

    public static final String EVERY_DAY_AT_09_PM = "0 0 21 * * *";

    private final BulkWorkspaceCrawlingService crawlingService;

    public ScanScheduler(BulkWorkspaceCrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    @Scheduled(cron = EVERY_DAY_AT_09_PM)
    public void check() {
        log.info("Starting crawl");
        crawlingService.crawlAllWorkspaces();
    }

}