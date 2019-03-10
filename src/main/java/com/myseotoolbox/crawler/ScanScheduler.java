package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@Profile("dev")
@Slf4j
//@RequestMapping("/crawl")
public class ScanScheduler {


    public static final String EVERY_DAY_AT_09_PM = "0 0 21 * * *";

    private final WorkspaceCrawler crawler;

    public ScanScheduler(WorkspaceCrawler crawler) {
        this.crawler = crawler;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void check() {

        log.info("Starting crawl");
        crawler.crawlAllWorkspaces();

    }


}