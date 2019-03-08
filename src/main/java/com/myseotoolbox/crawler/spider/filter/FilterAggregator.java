package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterAggregator implements UriFilter {
    private final List<UriFilter> predicates;

    public FilterAggregator(UriFilter... predicates) {
        this.predicates = Stream.of(predicates).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return predicates.stream().allMatch(uriPredicate -> uriPredicate.shouldCrawl(sourceUri, discoveredLink));
    }
}
