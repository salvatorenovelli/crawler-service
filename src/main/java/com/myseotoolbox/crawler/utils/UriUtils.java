package com.myseotoolbox.crawler.utils;

public class UriUtils {
    public static boolean isValidUri(String url) {
        return !url.trim().isEmpty() && !url.startsWith("javascript:");
    }
}
