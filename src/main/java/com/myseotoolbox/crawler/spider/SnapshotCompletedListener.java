package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.PageSnapshot;

public interface SnapshotCompletedListener {
    void onSnapshotComplete(PageSnapshot snapshot);
}
