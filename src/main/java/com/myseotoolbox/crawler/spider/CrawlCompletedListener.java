package com.myseotoolbox.crawler.spider;

import java.net.URI;
import java.util.List;

public interface CrawlCompletedListener {
    void onSnapshotComplete(URI uri, List<URI> links);
}
