package com.myseotoolbox.crawl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Profile("!dev")
@Slf4j
public class ScanScheduler {


    public static final String EVERY_DAY_AT_09_PM = "0 0 21 * * *";
    private final MonitoredUriScanner monitoredUriScanner;

    public ScanScheduler(MonitoredUriScanner monitoredUriScanner) {
        this.monitoredUriScanner = monitoredUriScanner;
    }

//    @Scheduled(cron = EVERY_DAY_AT_09_PM)
    public void check() {
        log.info("Starting scheduled scan");
        monitoredUriScanner.scanAll();
        log.info("Finished scheduled scan");
    }

}