package com.myseotoolbox.crawler.model;


import lombok.Getter;

@Getter
public class SnapshotResult {

    private final String uri;
    private final boolean isBlockedChain;
    private final RedirectChain chain;
    private final PageSnapshot pageSnapshot;

    private SnapshotResult(String uri, boolean isBlockedChain, PageSnapshot snapshot, RedirectChain chain) {
        this.uri = uri;
        this.isBlockedChain = isBlockedChain;
        this.pageSnapshot = snapshot;
        this.chain = chain;
    }

    public static SnapshotResult forSnapshot(PageSnapshot snapshot) {
        return new SnapshotResult(snapshot.getUri(), false, snapshot, null);
    }

    public static SnapshotResult forBlockedChain(RedirectChain chain) {
        return new SnapshotResult(chain.getElements().get(0).getSourceURI(), true, null, chain);
    }

    public String getUri() {
        return uri;
    }

}
