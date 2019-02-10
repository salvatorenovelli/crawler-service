package com.myseotoolbox.crawler.httpclient;

import com.myseotoolbox.crawler.model.PageSnapshot;
import lombok.Getter;

@Getter
public class SnapshotException extends Exception {
    private final PageSnapshot partialSnapshot;

    public SnapshotException(Exception e, PageSnapshot partialSnapshot) {
        super(e);
        this.partialSnapshot = partialSnapshot;
    }
}
