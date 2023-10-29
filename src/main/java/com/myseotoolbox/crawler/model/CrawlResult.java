package com.myseotoolbox.crawler.model;


import lombok.Data;

@Data
public class CrawlResult {

    private final String uri;
    private final boolean isBlockedChain;
    private final RedirectChain chain;
    private final PageSnapshot pageSnapshot;

    private CrawlResult(String uri, boolean isBlockedChain, PageSnapshot snapshot, RedirectChain chain) {
        this.uri = uri;
        this.isBlockedChain = isBlockedChain;
        this.pageSnapshot = snapshot;
        this.chain = chain;
    }

    public static CrawlResult forSnapshot(PageSnapshot snapshot) {
        return new CrawlResult(snapshot.getUri(), false, snapshot, null);
    }

    public static CrawlResult forBlockedChain(RedirectChain chain) {
        return new CrawlResult(chain.getElements().get(0).getSourceURI(), true, null, chain);
    }

}
