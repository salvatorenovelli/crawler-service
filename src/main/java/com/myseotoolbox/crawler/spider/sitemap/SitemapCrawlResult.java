package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;

import java.util.List;


public record SitemapCrawlResult(WebsiteCrawl crawl, List<SiteMap> sitemaps) { }