package com.myseotoolbox.crawler.testutils.testwebsite;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter@Setter
class Page {
    private final String pagePath;
    private int status = 200;
    private String title;
    private String metaDescription;
    private Map<String, List<String>> tags = new HashMap<>();
    private String redirectUri;
    private String mimeType = "text/html";


    public Page(String pagePath) {
        this.pagePath = pagePath;
    }


    public void setAsRedirect(int status, String dstUri) {
        this.status = status;
        this.redirectUri = dstUri;
    }

    public void addTag(String tagName, String content) {
        this.tags.computeIfAbsent(tagName, k -> new ArrayList<>());
        this.tags.get(tagName).add(content);
    }
}
