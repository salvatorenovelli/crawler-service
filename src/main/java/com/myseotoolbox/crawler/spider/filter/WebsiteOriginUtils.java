package com.myseotoolbox.crawler.spider.filter;

import java.net.URI;

public class WebsiteOriginUtils {
    public static String extractHostPort(URI uri) {
        String portStr = "";
        if (uri.getPort() != -1 && uri.getPort() != 80) {
            portStr = "" + uri.getPort();
        }
        return uri.getHost() + portStr;
    }
}
