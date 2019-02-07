package com.myseotoolbox.crawl.httpclient;


import com.myseotoolbox.crawl.CalendarService;
import com.myseotoolbox.crawl.PageCrawlPersistence;
import com.myseotoolbox.crawl.model.MonitoredUri;
import com.myseotoolbox.crawl.model.PageSnapshot;
import com.myseotoolbox.crawl.repository.PageSnapshotRepository;
import io.vavr.CheckedRunnable;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Date;

@Slf4j
@Component
public class WebPageScraper {


    private final WebPageReader webPageReader;
    private final PageSnapshotRepository pageSnapshotRepository;
    private final PageCrawlPersistence pageCrawlPersistence;
    private final CalendarService calendarService;

    @Autowired
    public WebPageScraper(WebPageReader webPageReader, PageSnapshotRepository pageSnapshotRepository, PageCrawlPersistence pageCrawlPersistence, CalendarService calendarService) {
        this.webPageReader = webPageReader;
        this.pageSnapshotRepository = pageSnapshotRepository;
        this.pageCrawlPersistence = pageCrawlPersistence;
        this.calendarService = calendarService;
    }


    public MonitoredUri crawlUri(MonitoredUri monitoredUri) {

        Date scanDate = calendarService.now();
        PageSnapshot newValue = buildDefaultSnapshot(monitoredUri, scanDate);

        try {
            String uri = monitoredUri.getUri();
            log.info("Scanning {}", uri);
            newValue = webPageReader.snapshotPage(URI.create(uri));
            newValue.setCreateDate(scanDate);

        } catch (Exception e) {
            String message = "Error while crawling " + monitoredUri.getUri() + ": " + e.toString();
            newValue.setCrawlStatus(message);
            log.warn(message, e);
        }

        persistSnapshot(monitoredUri.getCurrentValue(), newValue);


        monitoredUri.setCurrentValue(newValue);
        monitoredUri.setLastScan(newValue.getCreateDate());

        return monitoredUri;
    }

    private void persistSnapshot(PageSnapshot prevValue, PageSnapshot newValue) {
        runOrLogError(() -> pageCrawlPersistence.persistPageCrawl(prevValue, newValue), "Error while persisting crawl");
        runOrLogError(() -> pageSnapshotRepository.save(newValue), "Error while persisting pageSnapshot");
    }

    private void runOrLogError(CheckedRunnable task, String errorPrefix) {
        Try.run(task).orElseRun(throwable -> log.warn(errorPrefix, throwable));
    }

    private PageSnapshot buildDefaultSnapshot(MonitoredUri monitoredUri, Date scanDate) {
        PageSnapshot newValue = new PageSnapshot();
        newValue.setUri(monitoredUri.getUri());
        newValue.setCreateDate(scanDate);
        return newValue;
    }
}