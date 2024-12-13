package com.myseotoolbox.crawler.testutils;

import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.CrawlExecutorFactory;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.SpiderConfig;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.spider.sitemap.SitemapService;
import com.myseotoolbox.crawler.utils.CurrentThreadCrawlExecutorFactory;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractOrigin;

public class TestCrawlJobBuilder {
    public static final List<Integer> EXPECTED_WORKSPACES_FOR_TRIGGER = Arrays.asList(1, 2, 3);
    private final CrawlExecutorFactory testExecutorBuilder = new CurrentThreadCrawlExecutorFactory();

    private final HttpURLConnectionFactory connectionFactory = new HttpURLConnectionFactory();
    private final HttpRequestFactory requestFactory = new HttpRequestFactory(connectionFactory);
    private final SitemapService sitemapService = new SitemapService(requestFactory);
    private CrawlEventDispatch crawlEventDispatch;
    private final CrawlEventDispatchFactory crawlEventDispatchFactory;

    public TestCrawlJobBuilder(CrawlEventDispatch crawlEventDispatch) {
        this.crawlEventDispatch = crawlEventDispatch;
        crawlEventDispatchFactory = null;
    }

    public TestCrawlJobBuilder(CrawlEventDispatchFactory factory) {
        this.crawlEventDispatch = null;
        crawlEventDispatchFactory = factory;
    }


    public CrawlJob buildForSeeds(List<URI> seeds) {

        //mimic BulkWorkspaceCrawlingService

        URI origin = extractOrigin(seeds.get(0));
        SpiderConfig spiderConfig = new SpiderConfig();

        CrawlJobFactory crawlJobFactory = spiderConfig
                .getCrawlJobFactory(testExecutorBuilder, sitemapService);

        RobotsTxtAggregation robotsTxtAggregation = new RobotsTxtAggregation(new HTTPClient());

        RobotsTxt merged = robotsTxtAggregation.mergeConfigurations(seeds.stream().map(uri -> {
            Workspace workspace = new Workspace();
            workspace.setWebsiteUrl(uri.toString());
            return workspace;
        }).collect(Collectors.toList()));

        CrawlJobConfiguration conf = CrawlJobConfiguration
                .newConfiguration(TestWebsiteCrawlFactory.TEST_OWNER, origin)
                .withTriggerForScheduledScanOn(EXPECTED_WORKSPACES_FOR_TRIGGER)
                .withSeeds(seeds)
                .withConcurrentConnections(seeds.size())
                .withRobotsTxt(merged)
                .build();

        if (crawlEventDispatch == null) {
            crawlEventDispatch = crawlEventDispatchFactory.buildFor(conf.getWebsiteCrawl());
        }


        return crawlJobFactory.build(conf, crawlEventDispatch);
    }
}
