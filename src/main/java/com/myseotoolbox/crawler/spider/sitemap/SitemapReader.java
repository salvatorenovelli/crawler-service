package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.PathMatcher;
import com.myseotoolbox.crawler.spider.UriFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SitemapReader {
    /*
     * There are sitemaps with millions of entries.
     * @param uriFilter: make sure we only fetch the sitemaps we need (for discovered sitemaps), and filters the ones provided in the sitemapUrls (that come from robots.txt)
     *
     * */
    public List<URI> fetchSeedsFromSitemaps(URI origin, List<String> sitemapsUrls, UriFilter uriFilter, int crawledPageLimit) {

        log.info("Fetching {} sitemaps for {} with filter: {}. Urls: {}", sitemapsUrls.size(), origin, uriFilter, sitemapsUrls);

        List<URI> sitemapSeeds = new SiteMap(origin, sitemapsUrls, uriFilter, crawledPageLimit)
                .fetchUris()
                .stream()
                .map(this::toValidUri)
                .filter(Optional::isPresent).map(Optional::get)
                .filter(uri -> uriFilter.shouldCrawl(origin, uri))
                .collect(Collectors.toList());

        log.info("Found {} seeds from sitemap for {}", sitemapSeeds.size(), origin);
        log.debug("{} seeds: {}", origin, sitemapSeeds);
        return sitemapSeeds;
    }

    private Optional<URI> toValidUri(String s) {
        try {
            return Optional.of(URI.create(s));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
