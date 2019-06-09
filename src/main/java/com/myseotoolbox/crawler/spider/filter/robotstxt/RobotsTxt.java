package com.myseotoolbox.crawler.spider.filter.robotstxt;

import com.myseotoolbox.crawler.spider.UriFilter;

import java.util.List;

public interface RobotsTxt extends UriFilter {
    List<String> getSitemaps();
}
