package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;

public class WebPageReaderFactory {
    public WebPageReader build(UriFilter uriFilter) {
        return new WebPageReader(uriFilter);
    }
}
