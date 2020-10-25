package com.myseotoolbox.crawler.pagelinks;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;


/**
 * Represent a link to another page.
 * Can be any type of link, coming from hyperlinks <a href=...>, as well as links in canonical, sitemap, etc...
 */
@RequiredArgsConstructor
@Data
public class PageLink {
    private final String destination;
    private final Map<String, String> attributes;
}
