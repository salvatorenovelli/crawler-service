package com.myseotoolbox.crawlercommons;

import com.google.common.net.UrlEscapers;

import java.net.URI;
import java.net.URISyntaxException;

public class UriCreator {
    public static URI create(String link) throws URISyntaxException {
        String escaped = UrlEscapers.urlFragmentEscaper().escape(link);
        return new URI(escaped);
    }
}
