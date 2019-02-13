package com.myseotoolbox.crawler.spider.filter;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class FilterAggregator implements Predicate<URI> {
    private final List<Predicate<URI>> predicates;

    public FilterAggregator(Predicate<URI>... predicates) {
        this.predicates = Arrays.asList(predicates);
    }

    @Override
    public boolean test(URI uri) {
        return predicates.stream().allMatch(uriPredicate -> uriPredicate.test(uri));
    }
}
