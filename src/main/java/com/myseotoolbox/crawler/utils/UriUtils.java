package com.myseotoolbox.crawler.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UriUtils {
    public static boolean isValidUri(String url) {
        return !url.trim().isEmpty() && !url.startsWith("javascript:");
    }

    public static String getFolder(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String fullPath = url.getPath();

        int lastSlashIndex = fullPath.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            return fullPath.substring(0, lastSlashIndex + 1);
        } else {
            return "/";
        }
    }
}
