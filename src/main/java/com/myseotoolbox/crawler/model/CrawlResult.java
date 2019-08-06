package com.myseotoolbox.crawler.model;


import lombok.Getter;

import java.net.URI;

@Getter
public class CrawlResult {

    private final String uri;
    private final boolean isBlockedChain;
    private final RedirectChain chain;
    private final PageSnapshot pageSnapshot;
    private final URI crawlOrigin;

    private CrawlResult(URI crawlOrigin, String uri, boolean isBlockedChain, PageSnapshot snapshot, RedirectChain chain) {
        this.crawlOrigin = crawlOrigin;
        this.uri = uri;
        this.isBlockedChain = isBlockedChain;
        this.pageSnapshot = snapshot;
        this.chain = chain;
    }

    public static CrawlResult forSnapshot(URI crawlOrigin, PageSnapshot snapshot) {
        return new CrawlResult(crawlOrigin, snapshot.getUri(), false, snapshot, null);
    }

    public static CrawlResult forBlockedChain(URI crawlOrigin, RedirectChain chain) {
        return new CrawlResult(crawlOrigin, chain.getElements().get(0).getSourceURI(), true, null, chain);
    }

    public String getUri() {
        return uri;
    }

}
