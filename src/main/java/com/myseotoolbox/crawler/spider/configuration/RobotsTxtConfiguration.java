package com.myseotoolbox.crawler.spider.configuration;

import java.net.URI;

public class RobotsTxtConfiguration {
    private final URI origin;

    public RobotsTxtConfiguration(URI origin) {
        this.origin = origin;
    }

    public URI getOrigin() {
        return origin;
    }
}
