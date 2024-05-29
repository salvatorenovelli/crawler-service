package com.myseotoolbox.crawler.spider.configuration;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AllowedPathFromSeeds {

    /**
     * Hack?
     * <p>
     * Instead of interpreting seeds as filters, we should ask the user for filters.      *
     * Or should we? The user might not care or know. This is how it works in most of the crawlers.
     */
    public static List<String> extractAllowedPathFromSeeds(Collection<URI> seeds) {
        return seeds.stream()
                .map(URI::getPath)
                .map(AllowedPathFromSeeds::extractAllowedPathFromSeed)
                .collect(Collectors.toList());
    }

    public static String extractAllowedPathFromSeed(String path) {
        return removeFilename(addSlashIfEmpty(path));
    }

    private static String removeFilename(String path) {
        if (path.endsWith("/")) return path;
        return path.substring(0, path.lastIndexOf("/") + 1);
    }

    private static String addSlashIfEmpty(String input) {
        return StringUtils.isEmpty(input) ? "/" : input;
    }
}
