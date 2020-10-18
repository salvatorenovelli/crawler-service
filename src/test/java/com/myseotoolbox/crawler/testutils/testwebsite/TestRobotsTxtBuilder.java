package com.myseotoolbox.crawler.testutils.testwebsite;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TestRobotsTxtBuilder {
    private final TestWebsiteBuilder parent;
    private final Consumer<String> contentConsumer;
    private String currentUserAgent = "*";
    private Map<String, List<String>> rules = new HashMap<>();
    private List<String> sitemaps = new ArrayList<>();

    public TestRobotsTxtBuilder(TestWebsiteBuilder parent) {
        this.parent = parent;
        this.contentConsumer = null;
    }

    public TestRobotsTxtBuilder(Consumer<String> contentConsumer) {
        this.contentConsumer = contentConsumer;
        this.parent = null;
    }

    public TestRobotsTxtBuilder userAgent(String userAgent) {
        this.currentUserAgent = userAgent;
        return this;
    }

    public TestRobotsTxtBuilder disallow(String path) {
        this.rules.computeIfAbsent(currentUserAgent, s -> new ArrayList<>()).add("Disallow: " + path);
        return this;
    }

    public TestRobotsTxtBuilder reportingSitemapOn(String ...locations) {
        this.sitemaps.addAll(Arrays.asList(locations));
        return this;
    }

    public TestWebsiteBuilder build() {
        String content = render();

        if (parent != null) parent.robotsTxtStream = IOUtils.toInputStream(content, Charsets.UTF_8);
        if (contentConsumer != null) contentConsumer.accept(content);

        return parent;
    }


    public String render() {
        String rulesTxt = rules.entrySet().stream().map(stringListEntry -> {
            String userAgent = "User-agent: " + stringListEntry.getKey() + "\n";
            String rules = stringListEntry.getValue().stream().map(s -> s + "\n").collect(Collectors.joining());
            return userAgent + rules;
        }).collect(Collectors.joining());

        String sitemapsTxt = sitemaps.stream().map(s -> "Sitemap: " + s).collect(Collectors.joining("\n"));


        return rulesTxt + "\n" + sitemapsTxt;
    }

    public TestRobotsTxtBuilder and() {
        return this;
    }
}
