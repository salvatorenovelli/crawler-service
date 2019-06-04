package com.myseotoolbox.crawler.spider.robotstxt;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class EmptyRobotsTxt implements RobotsTxt {
    private static final RobotsTxt INSTANCE = new EmptyRobotsTxt();

    private EmptyRobotsTxt() {}

    public static RobotsTxt instance() {
        return INSTANCE;
    }

    @Override
    public List<String> getSitemaps() {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return true;
    }
}
