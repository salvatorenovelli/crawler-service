package com.myseotoolbox.crawler.testutils.testwebsite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
@Setter
class Page {
    private final String pagePath;
    private int status = 200;
    private String title;
    private String metaDescription;
    private Map<String, List<String>> tags = new HashMap<>();
    private String redirectUri;
    private String mimeType = "text/html";
    private List<Link> links = new ArrayList<>();
    private boolean charsetFieldPresent;


    public Page(String pagePath) {
        this.pagePath = pagePath;
    }


    public void setAsRedirect(int status, String dstUri) {
        this.status = status;
        this.redirectUri = dstUri;
    }

    public void addLinks(List<String> links, String linkAttributes) {
        this.links.addAll(links.stream().map(s -> new Link(s, linkAttributes)).collect(Collectors.toList()));
    }

    public void addTag(String tagName, String content) {
        this.tags
                .computeIfAbsent(tagName, k -> new ArrayList<>())
                .add(content);
    }

}


@RequiredArgsConstructor
@Getter
class Link {
    private final String url;
    private final String attributes;
}