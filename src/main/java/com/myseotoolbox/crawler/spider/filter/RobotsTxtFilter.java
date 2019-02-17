package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import com.panforge.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class RobotsTxtFilter implements UriFilter {

    private final ConcurrentHashMap<URI, Boolean> cache = new ConcurrentHashMap<>();
    private final RobotsTxt robotsTxt;

    public RobotsTxtFilter(InputStream robotsTxtFileStream) throws IOException {
        robotsTxt = RobotsTxt.read(robotsTxtFileStream);
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return cache.computeIfAbsent(discoveredLink, uri1 -> robotsTxt.query(null, discoveredLink.getPath()));
    }
}
