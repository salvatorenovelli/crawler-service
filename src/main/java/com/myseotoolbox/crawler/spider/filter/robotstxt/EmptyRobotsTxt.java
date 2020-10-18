package com.myseotoolbox.crawler.spider.filter.robotstxt;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class EmptyRobotsTxt implements RobotsTxt {

    private final List<String> sitemaps;

    public EmptyRobotsTxt(URI websiteOrigin) {
        if (websiteOrigin == null) {
            sitemaps = Collections.emptyList();
        } else {
            sitemaps = Collections.singletonList(websiteOrigin.resolve("/sitemap.xml").toString());
        }
    }

    @Override
    public List<String> getSitemaps() {
        return sitemaps;
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return true;
    }
}
