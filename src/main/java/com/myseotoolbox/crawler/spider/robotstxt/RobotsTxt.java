package com.myseotoolbox.crawler.spider.robotstxt;

import com.myseotoolbox.crawler.spider.UriFilter;

import java.util.List;

public interface RobotsTxt extends UriFilter {
    List<String> getSitemaps();
}
