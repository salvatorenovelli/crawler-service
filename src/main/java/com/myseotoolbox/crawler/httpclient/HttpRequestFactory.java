package com.myseotoolbox.crawler.httpclient;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class HttpRequestFactory {
    private final HttpURLConnectionFactory connectionFactory;

    public HttpRequestFactory(HttpURLConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public HttpGetRequest buildGetFor(URI uri) {
        return new HttpGetRequest(uri, connectionFactory);
    }
}
