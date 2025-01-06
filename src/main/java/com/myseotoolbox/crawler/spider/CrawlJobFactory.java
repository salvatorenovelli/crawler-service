package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.config.CrawlersPoolFactory;
import com.myseotoolbox.crawler.config.WebPageReaderFactory;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapCrawlResult;
import com.myseotoolbox.crawler.spider.sitemap.SitemapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlJobFactory {

    private final WebPageReaderFactory webPageReaderFactory;
    private final WebsiteUriFilterFactory uriFilterFactory;
    private final CrawlExecutorFactory crawlExecutorFactory;
    private final CrawlEventDispatchFactory crawlEventDispatchFactory;
    private final CrawlersPoolFactory crawlersPoolFactory;
    private final SitemapService sitemapService;

    public CrawlJob make(CrawlJobConfiguration configuration) {
        log.debug("Building configuration for {}", configuration);

        URI origin = configuration.getOrigin();

        RobotsTxt robotsTxt = configuration.getRobotsTxt();
        //any changes to this filter needs to be duplicated in the sitemap filtering (duplicated logic ... sorry)
        UriFilter uriFilter = uriFilterFactory.build(origin, configuration.getAllowedPaths(), robotsTxt);
        log.info("robots.txt provided {} sitemaps", robotsTxt.getSitemaps().size());

        WebPageReader webPageReader = webPageReaderFactory.build(uriFilter, configuration.getCrawlDelayMillis());
        ThreadPoolExecutor executor = crawlExecutorFactory.buildExecutor(origin.getHost(), configuration.getMaxConcurrentConnections());
        CrawlersPool crawlersPool = crawlersPoolFactory.create(webPageReader, executor);
        CrawlEventDispatch dispatch = crawlEventDispatchFactory.buildFor(configuration.getWebsiteCrawl());

        List<URI> seeds = collectSeeds(configuration, uriFilter, origin);
        CrawlerQueue crawlerQueue = new CrawlerQueue(origin.getHost(), seeds, crawlersPool, uriFilter, configuration.getCrawledPageLimit(), dispatch);

        return new CrawlJob(configuration, crawlerQueue, executor, dispatch);
    }

    private List<URI> collectSeeds(CrawlJobConfiguration configuration, UriFilter uriFilter, URI origin) {
        SitemapCrawlResult sitemapCrawlResult = sitemapService.fetchSeedsFromSitemaps(configuration, uriFilter);
        Collection<URI> seedsFromConfig = configuration.getSeeds();
        List<URI> seedsFromSitemap = sitemapCrawlResult.sitemaps().stream().flatMap(siteMap -> siteMap.links().stream()).toList();
        List<URI> allSeeds = concat(seedsFromConfig, seedsFromSitemap);
        return removeSeedsOutsideOrigin(origin, allSeeds);
    }

    private List<URI> concat(Collection<URI> seeds, Collection<URI> seedsFromSitemap) {
        return Stream.concat(seeds.stream(), seedsFromSitemap.stream()).collect(Collectors.toList());
    }

    private List<URI> removeSeedsOutsideOrigin(URI origin, Collection<URI> seeds) {
        List<URI> filtered = seeds.stream().filter(u -> WebsiteOriginUtils.isHostMatching(origin, u)).collect(Collectors.toList());
        if (filtered.size() != seeds.size())
            log.warn("Seeds from external domains found on {}. Original Seeds: {} Filtered Seeds: {}", origin, seeds.size(), filtered.size());
        return filtered;
    }

}