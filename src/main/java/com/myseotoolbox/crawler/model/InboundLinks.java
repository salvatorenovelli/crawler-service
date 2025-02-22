package com.myseotoolbox.crawler.model;

import com.google.common.collect.ImmutableSet;
import com.myseotoolbox.crawler.pagelinks.LinkType;

import java.net.URI;
import java.util.Map;
import java.util.Set;


public class InboundLinks {
    private Map<LinkType, Set<URI>> internal;

    public Set<URI> getInternal(LinkType linkType) {
        if (internal.containsKey(linkType)) {
            return ImmutableSet.copyOf(internal.get(linkType));
        }
        return ImmutableSet.of();
    }
}