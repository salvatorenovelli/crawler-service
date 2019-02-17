package com.myseotoolbox.crawler.spider;

import java.net.URI;

public interface UriFilter {
    /**
     * @param sourceUri      The URI where the links were discovered
     * @param discoveredLink The discovered link
     */
    boolean shouldCrawl(URI sourceUri, URI discoveredLink);
}
