package com.myseotoolbox.crawler.testutils;

import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.CrawlExecutorFactory;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.SpiderConfig;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import com.myseotoolbox.crawler.utils.CurrentThreadCrawlExecutorFactory;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractOrigin;

public class TestCrawlJobBuilder {
    private final CrawlExecutorFactory testExecutorBuilder = new CurrentThreadCrawlExecutorFactory();
    private final SitemapReader sitemapReader = new SitemapReader();
    private final CrawlEventDispatch crawlEventDispatch;

    public TestCrawlJobBuilder(CrawlEventDispatch crawlEventDispatch) {
        this.crawlEventDispatch = crawlEventDispatch;
    }


    public CrawlJob buildForSeeds(List<URI> seeds) {

        //mimic WorkspaceCrawler

        URI origin = extractOrigin(seeds.get(0));
        SpiderConfig spiderConfig = new SpiderConfig();

        CrawlJobFactory crawlJobFactory = spiderConfig
                .getCrawlJobFactory(testExecutorBuilder, sitemapReader);

        RobotsTxtAggregation robotsTxtAggregation = new RobotsTxtAggregation(new HTTPClient());

        RobotsTxt merged = robotsTxtAggregation.mergeConfigurations(seeds.stream().map(uri -> {
            Workspace workspace = new Workspace();
            workspace.setWebsiteUrl(uri.toString());
            return workspace;
        }).collect(Collectors.toList()));

        CrawlJobConfiguration conf = CrawlJobConfiguration
                .newConfiguration(origin)
                .withSeeds(seeds)
                .withConcurrentConnections(seeds.size())
                .withRobotsTxt(merged)
                .build();


        return crawlJobFactory.build(conf, crawlEventDispatch);
    }
}
