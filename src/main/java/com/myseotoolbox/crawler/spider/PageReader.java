package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.PageSnapshot;

import java.net.URI;

public interface PageReader {
    PageSnapshot snapshotPage(URI testUri);
}
