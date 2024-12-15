package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.filter.BasicUriFilter;
import com.myseotoolbox.crawler.spider.filter.FilterAggregator;
import com.myseotoolbox.crawler.spider.filter.PathFilter;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class WebsiteUriFilterFactory {
    public UriFilter build(URI origin, List<String> allowedPaths, RobotsTxt robotsTxt) {
        return buildUriFilter(origin, allowedPaths, robotsTxt);
    }

    private FilterAggregator buildUriFilter(URI websiteOrigin, List<String> allowedPaths, RobotsTxt robotsTxt) {
        PathFilter pathFilter = new PathFilter(allowedPaths);
        BasicUriFilter basicFilter = new BasicUriFilter(websiteOrigin);
        return new FilterAggregator(robotsTxt, basicFilter, pathFilter);
    }

}
