package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.filter.BasicUriFilter;
import com.myseotoolbox.crawler.spider.filter.FilterAggregator;
import com.myseotoolbox.crawler.spider.filter.PathFilter;
import com.myseotoolbox.crawler.spider.filter.RobotsTxtFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
public class WebsiteUriFilterFactory {
    public UriFilter build(URI origin, List<String> allowedPaths) {
        return buildUriFilter(origin, allowedPaths);
    }

    private FilterAggregator buildUriFilter(URI websiteOrigin, List<String> allowedPaths) {
        PathFilter pathFilter = new PathFilter(allowedPaths);
        RobotsTxtFilter robotsTxt = getRobotsTxtFilter(websiteOrigin);
        BasicUriFilter basicFilter = getBasicUriFilter(websiteOrigin);

        return new FilterAggregator(robotsTxt, basicFilter, (ignored, discoveredLink) -> pathFilter.shouldCrawl(discoveredLink.getPath()));
    }

    private BasicUriFilter getBasicUriFilter(URI websiteOrigin) {
        return new BasicUriFilter(websiteOrigin);
    }

    private RobotsTxtFilter getRobotsTxtFilter(URI websiteOrigin) {
        try {
            return new RobotsTxtFilter(websiteOrigin);
        } catch (IOException e) {
            log.warn("Unable to download robots.txt for website {}. Exception: {}", websiteOrigin, e.toString());
            return null;
        }
    }
}
