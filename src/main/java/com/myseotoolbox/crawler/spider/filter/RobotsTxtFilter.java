package com.myseotoolbox.crawler.spider.filter;

import com.panforge.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;


@Slf4j
public class RobotsTxtFilter implements Predicate<URI> {

    private final ConcurrentHashMap<URI, Boolean> cache = new ConcurrentHashMap<>();
    private final RobotsTxt robotsTxt;

    public RobotsTxtFilter(InputStream robotsTxtFileStream) throws IOException {
        robotsTxt = RobotsTxt.read(robotsTxtFileStream);
    }

    @Override
    public boolean test(URI uri) {
        return cache.computeIfAbsent(uri, uri1 -> robotsTxt.query(null, uri.getPath()));
    }
}
