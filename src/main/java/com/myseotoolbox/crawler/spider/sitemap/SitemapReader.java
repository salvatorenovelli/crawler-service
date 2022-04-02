package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.PathMatcher;
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
     * @param allowedPaths: make sure we only fetch the sitemap indexes we need (for discovered sitemaps), and filters the ones provided in the sitemapUrls (that come from robots.txt)
     *
     * */
    public List<URI> fetchSeedsFromSitemaps(URI origin, List<String> sitemapsUrls, List<String> allowedPaths) {

        List<String> filteredSitemapUrls = sitemapsUrls.stream()
                .filter(sitemapUrl -> PathMatcher.isSubPath(allowedPaths, URI.create(sitemapUrl).getPath()))
                .collect(Collectors.toList());

        log.info("Fetching {} sitemap for {} with allowed paths: {}. Urls: {}", filteredSitemapUrls.size(), origin, allowedPaths, filteredSitemapUrls);

        List<URI> sitemapSeeds = new SiteMap(origin, filteredSitemapUrls, allowedPaths)
                .fetchUris()
                .stream()
                .map(this::toValidUri)
                .filter(Optional::isPresent).map(Optional::get)
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
