package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.SnapshotResult;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Slf4j
public class CrawlersPool implements Consumer<SnapshotTask> {

    private final ExecutorService executor;
    private final WebPageReader pageReader;

    public CrawlersPool(WebPageReader pageReader, ExecutorService executor) {
        this.pageReader = pageReader;
        this.executor = executor;
    }

    @Override
    public void accept(SnapshotTask task) {
        executor.submit(() -> {
            try {
                try {
                    SnapshotResult result = pageReader.snapshotPage(task.getUri());
                    task.getTaskRequester().accept(result);
                } catch (SnapshotException e) {
                    log.warn("Unable to crawl: {}. Exception: {}", task.getUri(), e.getMessage());
                    task.getTaskRequester().accept(SnapshotResult.forSnapshot(e.getPartialSnapshot()));
                }
            } catch (Exception e) {
                log.error("Exception while crawling: " + task.getUri(), e);
            }
        });
    }
}
