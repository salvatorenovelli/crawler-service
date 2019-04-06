package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.filter.BasicUriFilter;
import com.myseotoolbox.crawler.spider.filter.FilterAggregator;
import com.myseotoolbox.crawler.spider.filter.RobotsTxtFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class WebsiteUriFilterBuilder {
    public UriFilter buildForOrigin(URI origin) {
        return buildUriFilter(origin);
    }

    private FilterAggregator buildUriFilter(URI websiteOrigin) {
        RobotsTxtFilter robotsTxt = getRobotsTxtFilter(websiteOrigin);
        BasicUriFilter basicFilter = getBasicUriFilter(websiteOrigin);

        return new FilterAggregator(robotsTxt, basicFilter);
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
