package com.myseotoolbox.crawler.spider;

import org.springframework.util.AntPathMatcher;

import java.util.List;

import static com.myseotoolbox.crawler.spider.configuration.AllowedPathFromSeeds.extractAllowedPathFromSeed;

public class PathMatcher {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static boolean isSubPath(String allowedPath, String possibleSubPath) {

        if (allowedPath.length() == 0) allowedPath = "/";
        if (possibleSubPath.length() == 0) possibleSubPath = "/";

        return matcher.match(toAntFilter(extractAllowedPathFromSeed(allowedPath)), possibleSubPath);
    }

    public static boolean isSubPath(List<String> allowedPaths, String possibleSubPath) {
        return allowedPaths.stream().anyMatch(allowedPath -> PathMatcher.isSubPath(allowedPath, possibleSubPath));
    }

    private static String toAntFilter(String s) {
        return s + "**";
    }

}
