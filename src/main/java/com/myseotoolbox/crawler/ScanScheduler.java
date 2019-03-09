package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;


@Component
@Profile("dev")
@Slf4j
//@RequestMapping("/crawl")
public class ScanScheduler {


    public static final String EVERY_DAY_AT_09_PM = "0 0 21 * * *";
    private final CrawlJobFactory factory;

    public ScanScheduler(CrawlJobFactory factory) {
        this.factory = factory;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void check() {

        log.info("Starting crawl");

        CrawlJob job = factory.build(URI.create("https://testhost"), Collections.emptyList(), 10);
        job.start();

    }


}