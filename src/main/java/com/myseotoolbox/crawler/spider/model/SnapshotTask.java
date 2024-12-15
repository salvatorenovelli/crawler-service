package com.myseotoolbox.crawler.spider.model;

import com.myseotoolbox.crawler.model.CrawlResult;
import lombok.Data;

import java.net.URI;
import java.util.function.Consumer;


public record SnapshotTask(URI uri, Consumer<CrawlResult> taskRequester) { }
