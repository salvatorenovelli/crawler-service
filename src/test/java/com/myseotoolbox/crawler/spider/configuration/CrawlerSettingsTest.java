package com.myseotoolbox.crawler.spider.configuration;

import org.junit.Test;

import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.MAX_CRAWL_DELAY_MILLIS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CrawlerSettingsTest {

    @Test
    public void shouldGetDefaultRateLimitingSetting() {
        CrawlerSettings crawlerSettings = new CrawlerSettings(null, true, null, null, null, null);
        assertThat(crawlerSettings.getCrawlDelayMillis(), is(0L));
    }

    @Test
    public void shouldNotAllowMoreMas() {
        CrawlerSettings crawlerSettings = new CrawlerSettings(null, true, null, MAX_CRAWL_DELAY_MILLIS + 1, null, null);
        assertThat(crawlerSettings.getCrawlDelayMillis(), is(MAX_CRAWL_DELAY_MILLIS));
    }
}