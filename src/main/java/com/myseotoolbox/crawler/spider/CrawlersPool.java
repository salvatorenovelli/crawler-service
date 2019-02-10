package com.myseotoolbox.crawler.spider;

import java.net.URI;
import java.util.List;

public interface CrawlersPool {
    void submit(List<URI> newLinks);
}
