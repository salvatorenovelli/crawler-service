package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.filter.BasicUriFilter;
import com.myseotoolbox.crawler.spider.filter.FilterAggregator;
import com.myseotoolbox.crawler.spider.filter.RobotsTxtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;

import static com.google.common.collect.ImmutableList.of;
import static com.myseotoolbox.crawler.spider.ExecutorBuilder.buildExecutor;
import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;


@Component
//@Profile("!dev")
@Slf4j

//@RequestMapping("/crawl")
public class ScanScheduler {


    public static final String EVERY_DAY_AT_09_PM = "0 0 21 * * *";

    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;

    @Autowired
    public ScanScheduler(MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence) {
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
    }


    //    @Scheduled(cron = EVERY_DAY_AT_09_PM)
    //    @GetMapping


//    @EventListener(ApplicationReadyEvent.class)
    public void check() throws IOException {


        HashSet<PageSnapshot> crawled = new HashSet<>();

        log.info("Starting crawl");


        URI websiteOrigin = URI.create("https://testwebsite");

        FilterAggregator uriFilters = buildUriFilter(websiteOrigin);

        CrawlJob job = new CrawlJob(websiteOrigin, of(websiteOrigin), new WebPageReader(), uriFilters, buildExecutor(10));

        job.subscribeToCrawlCompleted(snapshot -> {
            if (!crawled.add(snapshot)) {
                log.error("Completing more than once: {}", snapshot.getUri());
            }

            runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
            runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
        });

        job.start();

    }


}