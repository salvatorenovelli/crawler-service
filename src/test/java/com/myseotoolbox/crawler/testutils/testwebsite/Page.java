package com.myseotoolbox.crawler.testutils.testwebsite;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;


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
    private List<String> links = new ArrayList<>();
    private boolean charsetFieldPresent;


    public Page(String pagePath) {
        this.pagePath = pagePath;
    }


    public void setAsRedirect(int status, String dstUri) {
        this.status = status;
        this.redirectUri = dstUri;
    }

    public void addLinks(String... links) {
        this.links.addAll(Arrays.asList(links));
    }

    public void addTag(String tagName, String content) {
        this.tags
                .computeIfAbsent(tagName, k -> new ArrayList<>())
                .add(content);
    }
}
