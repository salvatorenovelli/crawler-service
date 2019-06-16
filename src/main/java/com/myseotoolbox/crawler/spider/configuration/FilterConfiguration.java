package com.myseotoolbox.crawler.spider.configuration;

public class FilterConfiguration {
    private final boolean ignoreRobotsTxt;

    public FilterConfiguration(Boolean ignoreRobotsTxt) {
        this.ignoreRobotsTxt = ignoreRobotsTxt != null && ignoreRobotsTxt;
    }

    public boolean shouldIgnoreRobotsTxt() {
        return ignoreRobotsTxt;
    }
}
