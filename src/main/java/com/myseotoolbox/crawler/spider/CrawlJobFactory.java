package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;

import java.net.URI;
import java.util.List;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

public class CrawlJobFactory {

    private final WebPageReaderFactory webPageReaderFactory;
    private final WebsiteUriFilterBuilder uriFilterBuilder;
    private final ExecutorBuilder executorBuilder;
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;

    public CrawlJobFactory(
            WebPageReaderFactory webPageReaderFactory,
            WebsiteUriFilterBuilder uriFilterBuilder,
            ExecutorBuilder executorBuilder,
            MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence) {

        this.webPageReaderFactory = webPageReaderFactory;
        this.uriFilterBuilder = uriFilterBuilder;
        this.executorBuilder = executorBuilder;
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
    }

    public CrawlJob build(URI origin, List<URI> seeds, int numParallelConnection) {

        UriFilter uriFilter = uriFilterBuilder.buildForOrigin(origin);
        CrawlJob job = new CrawlJob(origin, seeds, webPageReaderFactory.buildWithFilter(uriFilter), uriFilter, executorBuilder.buildExecutor(origin.getHost(), numParallelConnection));

        job.subscribeToPageCrawled(snapshot -> {
            runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
            runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
        });

        return job;
    }
}
