package com.myseotoolbox.crawler.spider;

import org.springframework.util.AntPathMatcher;

import java.util.List;


public class PathMatcher {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static boolean isSubPath(String allowedPath, String possibleSubPath) {

        if (allowedPath.length() == 0) allowedPath = "/";
        if (possibleSubPath.length() == 0) possibleSubPath = "/";

        return matcher.match(toAntFilter(removeFilename(allowedPath)), possibleSubPath);
    }

    public static boolean isSubPath(List<String> allowedPaths, String possibleSubPath) {
        return allowedPaths.stream().anyMatch(allowedPath -> PathMatcher.isSubPath(allowedPath, possibleSubPath));
    }

    private static String toAntFilter(String s) {
        return s + "**";
    }

    private static String removeFilename(String path) {
        if (path.endsWith("/")) return path;
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

}
