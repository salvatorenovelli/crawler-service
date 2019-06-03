package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlPersistence;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.monitoreduri.MonitoredUriUpdater;
import com.myseotoolbox.crawler.spider.sitemap.SiteMap;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
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

    public CrawlJob build(URI origin, List<URI> seeds, int numParallelConnection, int maxCrawls) {

        String name = origin.getHost();
        List<String> allowedPaths = extractAllowedPathFromSeeds(seeds);

        UriFilter uriFilter = uriFilterFactory.build(origin, allowedPaths);
        WebPageReader webPageReader = webPageReaderFactory.build(uriFilter);
        ThreadPoolExecutor executor = crawlExecutorFactory.buildExecutor(name, numParallelConnection);

        List<URI> seedsFromSitemap = getSeedsFromSitemap(origin, allowedPaths);

        List<URI> allSeeds = concat(seeds, seedsFromSitemap);
        CrawlJob job = new CrawlJob(name, allSeeds, webPageReader, uriFilter, executor, maxCrawls);

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
    private List<String> extractAllowedPathFromSeeds(Collection<URI> seeds) {
        return seeds.stream().map(URI::getPath).collect(Collectors.toList());
    }

    private List<URI> concat(Collection<URI> seeds, Collection<URI> seedsFromSitemap) {
        return Stream.concat(seeds.stream(), seedsFromSitemap.stream()).collect(Collectors.toList());
    }

    private List<URI> getSeedsFromSitemap(URI origin, List<String> allowedPaths) {
        log.debug("Fetching seeds from sitemap for {} with allowed paths: {}", origin, allowedPaths);
        List<URI> sitemapSeeds = new SiteMap(origin.toString(), allowedPaths).getUris().stream().map(URI::create).collect(Collectors.toList());
        log.info("Found {} seeds for {}", sitemapSeeds.size(), origin);
        return sitemapSeeds;
    }
}
