package com.myseotoolbox.crawler.spider.filter.robotstxt;

import java.net.URI;
import java.util.List;

public class IgnoredRobotsTxt implements RobotsTxt {

    private final RobotsTxt robotsTxt;

    public IgnoredRobotsTxt(String websiteOrigin, byte[] content) {
        this.robotsTxt = new DefaultRobotsTxt(websiteOrigin, content);
    }

    @Override
    public List<String> getSitemaps() {
        return robotsTxt.getSitemaps();
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return true;
    }
}
