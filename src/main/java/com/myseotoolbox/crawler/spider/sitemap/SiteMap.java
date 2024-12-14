package com.myseotoolbox.crawler.spider.sitemap;

import java.net.URI;
import java.util.Collection;

public record SiteMap(URI location, Collection<URI> links) {
    @Override
    public String toString() {
        return "SiteMapData{" +
                "location=" + location +
                ", links=" + links.size() +
                '}';
    }
}