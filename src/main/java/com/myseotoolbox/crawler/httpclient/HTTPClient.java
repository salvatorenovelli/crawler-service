package com.myseotoolbox.crawler.httpclient;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;


@Component
public class HTTPClient {

    public String get(URI uri) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(uri);
            CloseableHttpResponse response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException(new ResponseStatusException(HttpStatus.resolve(response.getStatusLine().getStatusCode())));
            }
            final HttpEntity entity = response.getEntity();
            return IOUtils.toString(entity.getContent(), Charsets.UTF_8);
        }
    }
}
