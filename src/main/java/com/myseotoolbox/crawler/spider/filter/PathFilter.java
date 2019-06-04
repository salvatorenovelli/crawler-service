package com.myseotoolbox.crawler.spider.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PathFilter {

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
        return s + "/**";
    }

    public boolean shouldCrawl(String path) {
        boolean b = allowedPaths.stream().anyMatch(s -> matcher.match(s, path));
        if (!b) {
            log.debug("Blocked: PATH URI: {}", path);
        }
        return b;
    }
}
