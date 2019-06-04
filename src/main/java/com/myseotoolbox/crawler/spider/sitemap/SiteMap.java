package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.filter.PathFilter;
import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.UnknownFormatException;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.isHostMatching;


@Slf4j
public class SiteMap {

    private final PathFilter pathFilter;
    private final List<URL> siteMapsUrls;

    private SiteMapParser siteMapParser = new SiteMapParser();

    public SiteMap(String url) {
        this(Collections.singletonList(url), Collections.singletonList("/"));
    }

    public SiteMap(List<String> sitemaps, List<String> allowedPaths) {
        this.siteMapsUrls = sitemaps.stream().map(this::mapToUrlOrLogWarning).filter(Objects::nonNull).collect(Collectors.toList());
        this.pathFilter = new PathFilter(allowedPaths);
    }

    public List<String> getUris() {
        return this.siteMapsUrls.stream().flatMap(url -> extractUrls(url).stream()).distinct().collect(Collectors.toList());
    }

    /**
     * Recursively scan Sitemap Index (the type of sitemap that has links to other sitemaps) and collect urls
     */
    private List<String> extractUrls(URL url) {

        if (!shouldFetch(url)) {
            return Collections.emptyList();
        }

        try {
            log.info("Fetching sitemap on {}", url.toString());
            AbstractSiteMap asm = siteMapParser.parseSiteMap(url);
            if (asm instanceof SiteMapIndex) {
                SiteMapIndex smi = (SiteMapIndex) asm;
                return smi.getSitemaps().stream().flatMap(sm -> extractUrls(sm.getUrl()).stream()).distinct().collect(Collectors.toList());
            } else {
                crawlercommons.sitemaps.SiteMap sm = (crawlercommons.sitemaps.SiteMap) asm;
                return sm.getSiteMapUrls().stream().map(siteMapURL -> siteMapURL.getUrl().toString()).distinct().collect(Collectors.toList());
            }
        } catch (UnknownFormatException | IOException e) {
            log.warn("Error while fetching sitemap for {}. Error: {}", url, e.toString());
            return Collections.emptyList();
        }


    }

    private boolean shouldFetch(URL url) {
        try {
            return isSameDomain(url) && (this.siteMapsUrls.contains(url) || pathFilter.shouldCrawl(url.getPath()));
        } catch (IllegalArgumentException e) {
            log.warn("Unable to fetch sitemap on {}. {}", url, e.toString());
            return false;
        }
    }

    private boolean isSameDomain(URL url) {
        return isHostMatching(URI.create(url.toString()), URI.create(this.siteMapsUrls.get(0).toString()));
    }

    private URL mapToUrlOrLogWarning(String s) {
        return Try.of(() -> new URL(s)).onFailure(throwable -> log.warn("Unable to crawl sitemap on {}. Error: {}", s, throwable.toString())).getOrElse((URL) null);
    }
}
