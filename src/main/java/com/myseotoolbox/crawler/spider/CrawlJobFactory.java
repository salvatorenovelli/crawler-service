package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.config.PageCrawlListener;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CrawlJobFactory {

    private final WebPageReaderFactory webPageReaderFactory;
    private final WebsiteUriFilterFactory uriFilterFactory;
    private final CrawlExecutorFactory crawlExecutorFactory;
    private final RobotsTxtFactory robotsTxtFactory;
    private final SitemapReader sitemapReader;

    public CrawlJobFactory(WebPageReaderFactory webPageReaderFactory,
                           WebsiteUriFilterFactory uriFilterFactory,
                           CrawlExecutorFactory crawlExecutorFactory,
                           RobotsTxtFactory robotsTxtFactory,
                           SitemapReader sitemapReader) {
        this.webPageReaderFactory = webPageReaderFactory;
        this.uriFilterFactory = uriFilterFactory;
        this.crawlExecutorFactory = crawlExecutorFactory;
        this.robotsTxtFactory = robotsTxtFactory;
        this.sitemapReader = sitemapReader;
    }

    public CrawlJob build(URI origin, List<URI> seeds, int numParallelConnection, int maxCrawls, PageCrawlListener onPageCrawled) {

        String name = origin.getHost();
        List<String> allowedPaths = extractAllowedPathFromSeeds(seeds);

        RobotsTxt robotsTxt = robotsTxtFactory.buildRobotsTxtFor(origin);

        UriFilter uriFilter = uriFilterFactory.build(origin, allowedPaths, robotsTxt);
        WebPageReader webPageReader = webPageReaderFactory.build(uriFilter);
        ThreadPoolExecutor executor = crawlExecutorFactory.buildExecutor(name, numParallelConnection);

        List<URI> seedsFromSitemap = sitemapReader.getSeedsFromSitemaps(origin, robotsTxt.getSitemaps(), allowedPaths);

        List<URI> allSeeds = concat(seeds, seedsFromSitemap);

        CrawlJob crawlJob = new CrawlJob(origin, allSeeds, webPageReader, uriFilter, executor, maxCrawls);
        crawlJob.subscribeToPageCrawled(onPageCrawled);
        return crawlJob;
    }

    /**
     * Hack.
     * <p>
     * Instead of interpreting seeds as filters, we should ask the user for filters.      *
     * Or should we? The user might not care or know. This is how it works in most of the crawlers.
     */
    private List<String> extractAllowedPathFromSeeds(Collection<URI> seeds) {
        return seeds.stream().map(URI::getPath).map(this::normalize).collect(Collectors.toList());
    }

    private List<URI> concat(Collection<URI> seeds, Collection<URI> seedsFromSitemap) {
        return Stream.concat(seeds.stream(), seedsFromSitemap.stream()).collect(Collectors.toList());
    }

    private String normalize(String input) {
        return StringUtils.isEmpty(input) ? "/" : input;
    }

}
