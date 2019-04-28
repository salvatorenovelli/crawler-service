package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

public class CrawlJobFactory {

    private final WebPageReaderFactory webPageReaderFactory;
    private final WebsiteUriFilterFactory uriFilterFactory;
    private final CrawlExecutorFactory crawlExecutorFactory;
    private final MonitoredUriUpdater monitoredUriUpdater;
    private final PageCrawlPersistence crawlPersistence;

    public CrawlJobFactory(
            WebPageReaderFactory webPageReaderFactory,
            WebsiteUriFilterFactory uriFilterFactory,
            CrawlExecutorFactory crawlExecutorFactory,
            MonitoredUriUpdater monitoredUriUpdater, PageCrawlPersistence crawlPersistence) {

        this.webPageReaderFactory = webPageReaderFactory;
        this.uriFilterFactory = uriFilterFactory;
        this.crawlExecutorFactory = crawlExecutorFactory;
        this.monitoredUriUpdater = monitoredUriUpdater;
        this.crawlPersistence = crawlPersistence;
    }

    public CrawlJob build(URI origin, List<URI> seeds, int numParallelConnection) {

        List<String> allowedPaths = extractAllowedPathFromSeeds(seeds);

        UriFilter uriFilter = uriFilterFactory.build(origin, allowedPaths);
        WebPageReader webPageReader = webPageReaderFactory.build(uriFilter);
        ThreadPoolExecutor executor = crawlExecutorFactory.buildExecutor(origin.getHost(), numParallelConnection);

        CrawlJob job = new CrawlJob(seeds, webPageReader, uriFilter, executor);

        job.subscribeToPageCrawled(snapshot -> {
            runOrLogWarning(() -> monitoredUriUpdater.updateCurrentValue(snapshot), "Error while updating monitored uris for uri: " + snapshot.getUri());
            runOrLogWarning(() -> crawlPersistence.persistPageCrawl(snapshot), "Error while persisting crawl for uri: " + snapshot.getUri());
        });

        return job;
    }

    /**
     * Hack.
     * <p>
     * Instead of interpreting seeds as filters, we should ask the user for filters.      *
     * Or should we? The user might not care or know. This is how it works in most of the crawlers.
     */
    private ArrayList<String> extractAllowedPathFromSeeds(List<URI> seeds) {
        return seeds.stream().map(URI::getPath).collect(Collectors.toCollection(ArrayList::new));
    }
}
