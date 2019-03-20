package com.myseotoolbox.crawler.model;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import static com.myseotoolbox.crawler.model.CrawlerSettings.*;
import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;

@Component
public class CrawlerSettingsSanitizer extends AbstractMongoEventListener<Workspace> {


    @Override
    public void onBeforeConvert(BeforeConvertEvent<Workspace> event) {

        CrawlerSettings source = event.getSource().getCrawlerSettings();


        source.setCrawlEnabled(source.isCrawlEnabled());
        source.setMaxConcurrentConnections(ensureRange(source.getMaxConcurrentConnections(), MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS));
        source.setCrawlIntervalDays(ensureRange(source.getCrawlIntervalDays(), MIN_CRAWL_INTERVAL, MAX_CRAWL_INTERVAL));


        super.onBeforeConvert(event);
    }


}