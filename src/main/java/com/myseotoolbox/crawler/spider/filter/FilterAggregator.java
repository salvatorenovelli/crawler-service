package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class FilterAggregator implements UriFilter {
    private final List<UriFilter> predicates;

    public FilterAggregator(UriFilter... predicates) {
        this.predicates = Arrays.asList(predicates);
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return predicates.stream().allMatch(uriPredicate -> uriPredicate.shouldCrawl(sourceUri, discoveredLink));
    }
}
