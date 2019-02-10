package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;

import java.net.URI;
import java.util.concurrent.ExecutorService;

public class CrawlersPool {

    private final ExecutorService executor;
    private final WebPageReader pageReader;
    private final SnapshotCompletedListener snapshotCompletedListener;

    public CrawlersPool(WebPageReader pageReader,
                        SnapshotCompletedListener snapshotCompletedListener,
                        ExecutorService executor) {
        this.pageReader = pageReader;
        this.executor = executor;
        this.snapshotCompletedListener = snapshotCompletedListener;
    }

    public void submit(URI link) {
        executor.submit(() -> {
            try {
                PageSnapshot snapshot = pageReader.snapshotPage(link);
                snapshotCompletedListener.onSnapshotComplete(snapshot);
            } catch (SnapshotException e) {
                snapshotCompletedListener.onSnapshotComplete(e.getPartialSnapshot());
            }
        });
    }
}
