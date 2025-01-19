package com.myseotoolbox.crawler.spider.sitemap;

import java.net.URI;
import java.util.Set;

public record SiteMap(URI location, Set<URI> links) {
    @Override
    public String toString() {
        return "SiteMapData{" + "location=" + location + ", links=" + links.size() + '}';
    }
}