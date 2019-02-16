package com.myseotoolbox.crawler.spider.model;

import com.myseotoolbox.crawler.model.PageSnapshot;
import lombok.Data;

import java.net.URI;
import java.util.function.Consumer;


@Data
public class SnapshotTask {
    private final URI uri;
    private final Consumer<PageSnapshot> taskRequester;
}
