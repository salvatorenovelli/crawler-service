package com.myseotoolbox.crawler.utils;

public class RemoveUrlFragment {

    private static final String URL_FRAGMENT_PATTERN = "#(.*)";

    public static String removeFragment(String url) {
        return url.replaceAll(URL_FRAGMENT_PATTERN, "");
    }
}
