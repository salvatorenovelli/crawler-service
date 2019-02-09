package com.myseotoolbox.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data@AllArgsConstructor
public class CrawlerSettings {
    private boolean crawlEnabled;
    private Set<String> seeds;
    private Set<String> filters;
}
