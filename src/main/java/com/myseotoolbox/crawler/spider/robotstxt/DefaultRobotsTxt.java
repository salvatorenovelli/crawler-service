package com.myseotoolbox.crawler.spider.robotstxt;

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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultRobotsTxt implements RobotsTxt {

    private final ConcurrentHashMap<URI, Boolean> cache = new ConcurrentHashMap<>();
    private final SimpleRobotRules robotRules;

    public DefaultRobotsTxt(URI websiteOrigin) throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            HttpGet httpget = new HttpGet(websiteOrigin.resolve("/robots.txt"));
            CloseableHttpResponse response = httpclient.execute(httpget);
            final HttpEntity entity = response.getEntity();
            SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
            this.robotRules = parser.parseContent(websiteOrigin.toString(), IOUtils.toByteArray(entity.getContent()), null, "");
            if (robotRules.getRobotRules().size() < 1) log.warn("robots.txt was empty for {}", websiteOrigin);

        }

    }

    @Override
    public boolean shouldCrawl(URI ignored, URI discoveredLink) {
        Boolean allowed = cache.computeIfAbsent(discoveredLink, uri1 -> robotRules.isAllowed(discoveredLink.toString()));
        if (!allowed) log.debug("Blocked: ROBOTS  URI: {}", discoveredLink);
        return allowed;
    }

    public List<String> getSitemaps() {
        return this.robotRules.getSitemaps();
    }
}
