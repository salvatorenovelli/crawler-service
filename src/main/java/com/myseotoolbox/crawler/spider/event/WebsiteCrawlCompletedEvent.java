package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;

import java.time.Instant;

public record WebsiteCrawlCompletedEvent(WebsiteCrawl websiteCrawl, int visited, Instant timestamp) {
}