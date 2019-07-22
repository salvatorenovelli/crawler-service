package com.myseotoolbox.crawlercommons;

import com.google.common.net.UrlEscapers;

import java.net.URI;

public class UriCreator {
    public static URI create(String link) {
        String escaped = UrlEscapers.urlFragmentEscaper().escape(link);
        return URI.create(escaped);
    }
}
