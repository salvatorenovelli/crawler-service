package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.UnsupportedMimeTypeException;

import java.net.URI;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

@Slf4j
public class CrawlersPool implements Consumer<SnapshotTask> {

    private final ThreadPoolExecutor executor;
    private final URI crawlOrigin;
    private final WebPageReader pageReader;

    public CrawlersPool(URI crawlOrigin, WebPageReader pageReader, ThreadPoolExecutor executor) {
        this.crawlOrigin = crawlOrigin;
        this.pageReader = pageReader;
        this.executor = executor;
    }

    @Override
    public void accept(SnapshotTask task) {
        log.debug("Task submitted: {}", task.getUri());
        executor.submit(() -> {
            try {
                try {
                    CrawlResult result = pageReader.snapshotPage(task.getUri());
                    task.getTaskRequester().accept(result);
                } catch (SnapshotException e) {
                    logException(e, task.getUri());
                    task.getTaskRequester().accept(CrawlResult.forSnapshot(crawlOrigin, e.getPartialSnapshot()));
                }
            } catch (Exception e) {
                log.error("Exception while crawling: " + task.getUri(), e);
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
        if (executor.getQueue().size() > 0) throw new IllegalStateException("Crawler terminated with pending tasks!");
        executor.shutdown();
    }
}
