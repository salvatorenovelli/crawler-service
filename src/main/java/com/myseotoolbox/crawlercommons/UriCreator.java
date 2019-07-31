package com.myseotoolbox.crawlercommons;

import com.myseotoolbox.crawler.spider.PageLinksHelper;

import java.net.URI;

public class UriCreator {
    public static URI create(String link) {
        return PageLinksHelper.toValidUri(link).orElseThrow(() -> new IllegalArgumentException("Invalid url: '" + link + "'"));
    }
}
