package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import com.myseotoolbox.crawler.utils.PathMatcher;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PathFilter implements UriFilter {

    private final List<String> allowedPaths;

    public PathFilter(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths.stream()
                .map(this::validatePath)
                .collect(Collectors.toList());
    }

    private String validatePath(String s) {
        if (!s.startsWith("/")) throw new IllegalArgumentException("Relative path needed but found: '" + s + "'");
        if (s.contains(":")) throw new IllegalArgumentException("Invalid URL path: '" + s + "'");
        return s;
    }

    private boolean isWithinAllowedPaths(String path) {
        return allowedPaths.stream().anyMatch(s -> PathMatcher.isSubPath(s, path));
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        boolean b = isWithinAllowedPaths(sourceUri.getPath()) || isWithinAllowedPaths(discoveredLink.getPath());
        if (!b) {
            log.debug("Blocked: PATH | uri: '{}' source: '{}'", discoveredLink.getPath(), sourceUri.getPath());
        }
        return b;
    }
}
