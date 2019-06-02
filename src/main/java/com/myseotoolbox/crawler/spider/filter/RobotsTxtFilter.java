package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import static com.myseotoolbox.crawler.httpclient.HttpGetRequest.USER_AGENT;

@Slf4j
public class RobotsTxtFilter implements UriFilter {

    private final ConcurrentHashMap<URI, Boolean> cache = new ConcurrentHashMap<>();
    private final SimpleRobotRules robotRules;

    public RobotsTxtFilter(URI websiteOrigin) throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpGet httpget = new HttpGet(websiteOrigin.resolve("/robots.txt"));
            CloseableHttpResponse response = httpclient.execute(httpget);
            final HttpEntity entity = response.getEntity();
            SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
            this.robotRules = parser.parseContent(websiteOrigin.toString(), IOUtils.toByteArray(entity.getContent()), null, USER_AGENT);
            if (robotRules.getRobotRules().size() < 1) log.warn("robots.txt was empty for {}", websiteOrigin);

        }

    }

    @Override
    public boolean shouldCrawl(URI ignored, URI discoveredLink) {
        return cache.computeIfAbsent(discoveredLink, uri1 -> robotRules.isAllowed(discoveredLink.toString()));
    }
}
