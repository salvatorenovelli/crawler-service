package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.filter.PathFilter;
import com.myseotoolbox.crawlercommons.UriCreator;
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

    private final URI origin;
    private final PathFilter pathFilter;
    private final int crawledPageLimit;
    private final List<URL> siteMaps;

    private SiteMapParser siteMapParser = new SiteMapParser(false);

    private int currentUriCount = 0;


    public SiteMap(URI origin, List<String> sitemaps, List<String> allowedPaths, int crawledPageLimit) {
        this.origin = origin;
        this.siteMaps = sitemaps.stream().map(this::mapToUrlOrLogWarning).filter(Objects::nonNull).collect(Collectors.toList());
        this.pathFilter = new PathFilter(allowedPaths);
        this.crawledPageLimit = crawledPageLimit;

        boolean anyNonCrawlable = sitemaps.stream().anyMatch(it -> !pathFilter.shouldCrawl(origin, URI.create(it)));
        if (anyNonCrawlable) throw new IllegalArgumentException("Provided sitemaps should be within allowedPaths");
    }

    public List<String> fetchUris() {
        return this.siteMaps
                .stream()
                .flatMap(url -> fetch(url).stream())
                .filter(this::isSameDomain)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Recursively scan Sitemap Index (the type of sitemap that has links to other sitemaps) and collect urls
     */
    private List<String> fetch(URL url) {

        if (!shouldFetch(url)) {
            return Collections.emptyList();
        }
        if (currentUriCount >= crawledPageLimit) {
            log.warn("Sitemap {} was stopped from fetching {} as it's exceeding limit of {} urls. Current: {}", origin, url, crawledPageLimit, currentUriCount);
            return Collections.emptyList();
        }

        try {
            log.debug("Fetching sitemap on {}", url.toString());
            AbstractSiteMap asm = siteMapParser.parseSiteMap(url);
            if (asm instanceof SiteMapIndex) {
                SiteMapIndex smi = (SiteMapIndex) asm;
                return smi.getSitemaps().stream()
                        .flatMap(sm -> fetch(sm.getUrl()).stream())
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                crawlercommons.sitemaps.SiteMap sm = (crawlercommons.sitemaps.SiteMap) asm;
                List<String> uriList = sm.getSiteMapUrls().stream()
                        .map(siteMapURL -> siteMapURL.getUrl().toString())
                        .distinct()
                        .collect(Collectors.toList());

                if (currentUriCount + uriList.size() > crawledPageLimit) {
                    log.warn("Sitemap {}->{} contains more urls than the allowed limit {}/{}", origin, url, currentUriCount + uriList.size(), crawledPageLimit);
                    uriList = uriList.stream().limit(crawledPageLimit - currentUriCount).collect(Collectors.toList());
                }

                currentUriCount += uriList.size();
                return uriList;
            }
        } catch (UnknownFormatException | IOException e) {
            log.warn("Error while fetching sitemap for {}. Error: {}", url, e.toString());
            return Collections.emptyList();
        }


    }

    private boolean shouldFetch(URL url) {
        try {
            return isSameDomain(url) && (this.siteMaps.contains(url) || pathFilter.shouldCrawl(URI.create(url.toString()), URI.create(url.toString())));
        } catch (IllegalArgumentException e) {
            log.warn("Unable to fetch sitemap on {}. {}", url, e.toString());
            return false;
        }
    }

    private boolean isSameDomain(String url) {
        return isHostMatching(UriCreator.create(url), origin, false);
    }

    private boolean isSameDomain(URL url) {
        return isSameDomain(url.toString());
    }

    private URL mapToUrlOrLogWarning(String s) {
        return Try.of(() -> new URL(s)).onFailure(throwable -> log.warn("Unable to crawl sitemap on {}. Error: {}", s, throwable.toString())).getOrElse((URL) null);
    }
}
