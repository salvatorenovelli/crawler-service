package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.filter.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Component
public class RobotsTxtAggregation {

    private final HTTPClient httpClient;

    public RobotsTxtAggregation(HTTPClient httpClient) {
        this.httpClient = httpClient;
    }


    public RobotsTxt aggregate(Collection<Workspace> workspaces) {
        try {
            Workspace workspace0 = new ArrayList<>(workspaces).get(0);

            if (anyWorkspaceDisableRobots(workspaces))
                return EmptyRobotsTxt.instance();


            URI origin = WebsiteOriginUtils.extractRoot(URI.create(workspace0.getWebsiteUrl()));
            String content = httpClient.get(origin.resolve("/robots.txt"));
            return new DefaultRobotsTxt(origin.toString(), content.getBytes());
        } catch (IOException e) {
            log.warn("Unable to build RobotsTxt for {}. {}", workspaces, e.toString());
            return EmptyRobotsTxt.instance();
        }
    }

    private boolean anyWorkspaceDisableRobots(Collection<Workspace> workspaces) {
        return workspaces.stream()
                .map(Workspace::getCrawlerSettings).filter(Objects::nonNull)
                .map(CrawlerSettings::getFilterConfiguration).filter(Objects::nonNull)
                .anyMatch(FilterConfiguration::shouldIgnoreRobotsTxt);
    }
}
