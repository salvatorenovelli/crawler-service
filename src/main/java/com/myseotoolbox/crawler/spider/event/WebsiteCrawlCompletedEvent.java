package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;

import java.time.Instant;
/**
 * This event should be published AFTER all the monitored Uri discovered in this crawl have been persisted.
 *
 * See: {@link com.myseotoolbox.crawler.monitoreduri.MonitoredUriInboundLinkCountResetListener}
 *
 * */
public record WebsiteCrawlCompletedEvent(WebsiteCrawl websiteCrawl, int visited, Instant timestamp) {
}