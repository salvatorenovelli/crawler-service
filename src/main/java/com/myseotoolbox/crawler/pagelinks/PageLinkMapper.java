package com.myseotoolbox.crawler.pagelinks;

import java.util.List;
import java.util.stream.Collectors;

public class PageLinkMapper {
    public static List<String> toLinkUrls(List<PageLink> links) {
        return links.stream().map(PageLink::getDestination).collect(Collectors.toList());
    }
}