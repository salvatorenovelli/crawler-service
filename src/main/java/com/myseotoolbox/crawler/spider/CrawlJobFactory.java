package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapCrawlResult;
import com.myseotoolbox.crawler.spider.sitemap.SitemapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class CrawlJobFactory {

    private final WebPageReaderFactory webPageReaderFactory;
    private final WebsiteUriFilterFactory uriFilterFactory;
    private final CrawlExecutorFactory crawlExecutorFactory;
    private final SitemapService sitemapService;


    public CrawlJob build(CrawlJobConfiguration configuration, CrawlEventDispatch dispatch) {

        log.debug("Building configuration for {}", configuration);

        URI origin = configuration.getOrigin();

        Collection<URI> seeds = configuration.getSeeds();
        List<String> allowedPaths = configuration.getAllowedPaths();

        RobotsTxt robotsTxt = configuration.getRobotsTxt();
        //any changes to this filter needs to be duplicated in the sitemap filtering (duplicated logic ... sorry)
        UriFilter uriFilter = uriFilterFactory.build(origin, allowedPaths, robotsTxt);
        WebPageReader webPageReader = webPageReaderFactory.build(uriFilter, configuration.crawlDelayMillis());
        ThreadPoolExecutor executor = crawlExecutorFactory.buildExecutor(origin.getHost(), configuration.getMaxConcurrentConnections());

        log.info("robots.txt provided {} sitemaps", robotsTxt.getSitemaps().size());

        SitemapCrawlResult sitemapCrawlResult = sitemapService.fetchSeedsFromSitemaps(configuration, uriFilter);
        List<URI> seedsFromSitemap = sitemapCrawlResult.sitemaps().stream().flatMap(siteMap -> siteMap.links().stream()).toList();
        List<URI> allSeeds = concat(seeds, seedsFromSitemap);

        return new CrawlJob(configuration.getWebsiteCrawl(), origin, allSeeds, webPageReader, uriFilter, executor, configuration.getCrawledPageLimit(), dispatch);
    }

    private List<URI> concat(Collection<URI> seeds, Collection<URI> seedsFromSitemap) {
        return Stream.concat(seeds.stream(), seedsFromSitemap.stream()).collect(Collectors.toList());
    }

}
