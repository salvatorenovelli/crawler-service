package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PathFilter implements UriFilter {

    private final List<String> allowedPaths;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public PathFilter(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths.stream()
                .map(this::validatePath)
                .map(this::toAntFilter).collect(Collectors.toList());
    }

    private String validatePath(String s) {
        if (!s.startsWith("/")) throw new IllegalArgumentException("Relative path needed but found: '" + s + "'");
        if (s.contains(":")) throw new IllegalArgumentException("Invalid URL path: '" + s + "'");
        return s;
    }

    private String toAntFilter(String s) {
        return s + "**";
    }

    private boolean isWithinAllowedPaths(String path) {
        return allowedPaths.stream().anyMatch(s -> matcher.match(s, path));
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        boolean b = isWithinAllowedPaths(discoveredLink.getPath()) || isWithinAllowedPaths(sourceUri.getPath());
        if (!b) {
            log.debug("Blocked: PATH | uri: '{}' source: '{}'", discoveredLink.getPath(), sourceUri.getPath());
        }
        return b;
    }
}
