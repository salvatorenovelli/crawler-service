package com.myseotoolbox.crawler.spider.sitemap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SitemapReader {
    public List<URI> getSeedsFromSitemaps(URI origin, List<String> sitemapsUrl, List<String> allowedPaths) {
        log.info("Fetching {} sitemap for {} with allowed paths: {}", sitemapsUrl.size(), origin, allowedPaths);
        List<URI> sitemapSeeds = new SiteMap(origin, sitemapsUrl, allowedPaths).fetchUris()
                .stream()
                .map(this::toValidUri)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        log.info("Found {} seeds from sitemap for {}", sitemapSeeds.size(), origin);
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
