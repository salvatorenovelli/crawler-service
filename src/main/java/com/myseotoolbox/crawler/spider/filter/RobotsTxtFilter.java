package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import com.panforge.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class RobotsTxtFilter implements UriFilter {

    private final ConcurrentHashMap<URI, Boolean> cache = new ConcurrentHashMap<>();
    private final RobotsTxt robotsTxt;

    public RobotsTxtFilter(URI websiteOrigin) throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpGet httpget = new HttpGet(websiteOrigin.resolve("/robots.txt"));
            CloseableHttpResponse response = httpclient.execute(httpget);
            final HttpEntity entity = response.getEntity();
            final InputStream robotsTxtStream = entity.getContent();
            this.robotsTxt = RobotsTxt.read(robotsTxtStream);
            if (robotsTxt.getDisallowList("*").size() < 1) log.warn("robots.txt was empty for {}", websiteOrigin);

        }

    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {
        return cache.computeIfAbsent(discoveredLink, uri1 -> robotsTxt.query(null, discoveredLink.getPath()));
    }
}
