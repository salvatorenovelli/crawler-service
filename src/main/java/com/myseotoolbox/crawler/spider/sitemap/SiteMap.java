package com.myseotoolbox.crawler.spider.sitemap;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.UnknownFormatException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class SiteMap {
    private final URL url;
    private SiteMapParser siteMapParser = new SiteMapParser();

    public SiteMap(String url) throws MalformedURLException {
        this.url = new URL(url);
    }


    public List<String> getUris() {
        return extractUrls(this.url);
    }

    private List<String> extractUrls(URL url) {


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
}
