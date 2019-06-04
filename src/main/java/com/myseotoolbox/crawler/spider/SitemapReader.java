package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.sitemap.SiteMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SitemapReader {
    public List<URI> getSeedsFromSitemaps(URI origin, List<String> sitemapsUrl, List<String> allowedPaths) {
        log.info("Fetching {} sitemap for {} with allowed paths: {}", sitemapsUrl.size(), origin, allowedPaths);
        List<URI> sitemapSeeds = new SiteMap(sitemapsUrl, allowedPaths).getUris().stream().map(URI::create).collect(Collectors.toList());
        log.info("Found {} seeds from sitemap for {}", sitemapSeeds.size(), origin);
        return sitemapSeeds;
    }
}
