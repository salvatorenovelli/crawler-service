package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.UnsupportedMimeTypeException;

import java.net.URI;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class CrawlersPool implements Consumer<SnapshotTask> {

    private final WebPageReader pageReader;
    private final ThreadPoolExecutor executor;

    @Override
    public void accept(SnapshotTask task) {
        log.debug("Task submitted: {}", task.uri());
        executor.submit(() -> {
            try {
                try {
                    CrawlResult result = pageReader.snapshotPage(task.uri());
                    task.taskRequester().accept(result);
                } catch (SnapshotException e) {
                    logException(e, task.uri());
                    task.taskRequester().accept(CrawlResult.forSnapshot(e.getPartialSnapshot()));
                }
            } catch (Exception e) {
                log.error("Exception while crawling: " + task.uri(), e);
            }
        });
    }

    private void logException(SnapshotException e, URI uri) {
        if (e.getCause() instanceof UnsupportedMimeTypeException) {
            log.debug("Unable to crawl: {}. Exception: {}", uri, e.getMessage());
        } else {
            log.warn("Unable to crawl: {}. Exception: {}", uri, e.getMessage());
        }
    }

    public void shutDown() {
        log.info("Shutting down executor {}", executor);
        if (!executor.getQueue().isEmpty()) throw new IllegalStateException("Crawler terminated with pending tasks!");
        executor.shutdown();
    }
}
