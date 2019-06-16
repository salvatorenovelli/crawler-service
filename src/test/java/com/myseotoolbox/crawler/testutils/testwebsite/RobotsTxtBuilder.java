package com.myseotoolbox.crawler.testutils.testwebsite;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RobotsTxtBuilder {
    private final TestWebsiteBuilder parent;
    private final Consumer<String> contentConsumer;
    private String currentUserAgent = "*";
    private Map<String, List<String>> rules = new HashMap<>();

    public RobotsTxtBuilder(TestWebsiteBuilder parent) {
        this.parent = parent;
        this.contentConsumer = null;
    }

    public RobotsTxtBuilder(Consumer<String> contentConsumer) {
        this.contentConsumer = contentConsumer;
        this.parent = null;
    }

    public RobotsTxtBuilder userAgent(String userAgent) {
        this.currentUserAgent = userAgent;
        return this;
    }

    public RobotsTxtBuilder disallow(String path) {
        this.rules.computeIfAbsent(currentUserAgent, s -> new ArrayList<>()).add("Disallow: " + path);
        return this;
    }

    public TestWebsiteBuilder build() {
        String content = render();

        if (parent != null) parent.robotsTxtStream = IOUtils.toInputStream(content, Charsets.UTF_8);
        if (contentConsumer != null) contentConsumer.accept(content);

        return parent;
    }


    public String render() {
        return rules.entrySet().stream().map(stringListEntry -> {
            String userAgent = "User-agent: " + stringListEntry.getKey() + "\n";
            String rules = stringListEntry.getValue().stream().map(s -> s + "\n").collect(Collectors.joining());
            return userAgent + rules;
        }).collect(Collectors.joining());
    }

    public RobotsTxtBuilder and() {
        return this;
    }
}
