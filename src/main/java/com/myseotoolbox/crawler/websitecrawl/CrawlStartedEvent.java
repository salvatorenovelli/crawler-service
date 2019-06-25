package com.myseotoolbox.crawler.websitecrawl;


import lombok.Data;

import java.util.List;

@Data
public class CrawlStartedEvent {
    private final String origin;
    private final List<String> seeds;
}
