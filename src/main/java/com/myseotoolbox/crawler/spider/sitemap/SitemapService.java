package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.UriFilter;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SitemapService {
    /*
     * There are sitemaps with hundreds of thousands entries.
     * @param uriFilter: make sure we only fetch the sitemaps we need (for discovered sitemaps), and filters the ones provided in the sitemapUrls (that come from robots.txt)
     * */
    private final SitemapReaderFactory sitemapReaderFactory;
    private final SitemapRepository sitemapRepository;

    public SitemapCrawlResult fetchSeedsFromSitemaps(CrawlJobConfiguration configuration, UriFilter uriFilter) {

        log.info("Fetching {} sitemaps for {} with filter: {}. Urls: {}", configuration.getRobotsTxt().getSitemaps().size(),
                configuration.getOrigin(), uriFilter, configuration.getRobotsTxt().getSitemaps());

        List<SiteMap> siteMapData = sitemapReaderFactory
                .getSitemapReaderFor(configuration, uriFilter)
                .fetchSitemaps();

        SitemapCrawlResult sitemapCrawlResult = new SitemapCrawlResult(configuration.getWebsiteCrawl(), siteMapData);
        sitemapRepository.persist(sitemapCrawlResult);

        return sitemapCrawlResult;
    }

}
