package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.spider.filter.PathFilter;
import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.UnknownFormatException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.isHostMatching;


@Slf4j
public class SiteMap {
    private final URL url;
    private final PathFilter pathFilter;

    private SiteMapParser siteMapParser = new SiteMapParser();

    public SiteMap(String url) {
        this(url, Collections.singletonList("/"));
    }

    public SiteMap(String url, List<String> allowedPaths) {
        try {
            this.url = new URL(url);
            this.pathFilter = new PathFilter(allowedPaths);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getUris() {
        return extractUrls(this.url);
    }

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
            log.warn("Error while fetching {}. Error: {}", url, e.toString());
            return Collections.emptyList();
        }


    }

    private boolean shouldFetch(URL url) {
        return isSameDomain(url) && (url.getPath().equals("/sitemap.xml") || pathFilter.shouldCrawl(url.getPath()));
    }

    private boolean isSameDomain(URL url) {
        return isHostMatching(URI.create(url.toString()), URI.create(this.url.toString()));
    }
}
