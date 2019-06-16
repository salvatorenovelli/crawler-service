package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlListener;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import lombok.extern.slf4j.Slf4j;

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
    private final SitemapReader sitemapReader;

    public CrawlJobFactory(WebPageReaderFactory webPageReaderFactory,
                           WebsiteUriFilterFactory uriFilterFactory,
                           CrawlExecutorFactory crawlExecutorFactory,
                           SitemapReader sitemapReader) {
        this.webPageReaderFactory = webPageReaderFactory;
        this.uriFilterFactory = uriFilterFactory;
        this.crawlExecutorFactory = crawlExecutorFactory;
        this.sitemapReader = sitemapReader;
    }


    public CrawlJob build(CrawlJobConfiguration configuration, PageCrawlListener onPageCrawled) {

        URI origin = configuration.getOrigin();

        Collection<URI> seeds = configuration.getSeeds();
        List<String> allowedPaths = configuration.getAllowedPaths();

        RobotsTxt robotsTxt = configuration.getRobotsTxt();

        UriFilter uriFilter = uriFilterFactory.build(origin, allowedPaths, robotsTxt);
        WebPageReader webPageReader = webPageReaderFactory.build(uriFilter);
        ThreadPoolExecutor executor = crawlExecutorFactory.buildExecutor(origin.getHost(), configuration.getMaxConcurrentConnections());

        List<URI> seedsFromSitemap = sitemapReader.getSeedsFromSitemaps(origin, robotsTxt.getSitemaps(), allowedPaths);

        List<URI> allSeeds = concat(seeds, seedsFromSitemap);

        CrawlJob crawlJob = new CrawlJob(origin, allSeeds, webPageReader, uriFilter, executor, configuration.getCrawledPageLimit());
        crawlJob.subscribeToPageCrawled(onPageCrawled);
        return crawlJob;
    }

    private List<URI> concat(Collection<URI> seeds, Collection<URI> seedsFromSitemap) {
        return Stream.concat(seeds.stream(), seedsFromSitemap.stream()).collect(Collectors.toList());
    }


}
