package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.WebPageReader;

import java.net.URI;

public class WebPageReaderFactory {
    private final HttpRequestFactory httpRequestFactory;

    public WebPageReaderFactory(HttpRequestFactory httpRequestFactory) {this.httpRequestFactory = httpRequestFactory;}

    public WebPageReader build(URI crawlOrigin, UriFilter uriFilter) {
        return new WebPageReader(crawlOrigin, uriFilter, httpRequestFactory);
    }
}
