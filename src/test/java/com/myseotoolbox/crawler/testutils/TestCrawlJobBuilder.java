package com.myseotoolbox.crawler.testutils;

import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.TestCrawlJobFactoryBuilder;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractOrigin;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestCrawlJobBuilder {
    public static final List<Integer> EXPECTED_WORKSPACES_FOR_TRIGGER = Arrays.asList(1, 2, 3);
    private final CrawlEventDispatchFactory crawlEventDispatchFactory;

    public TestCrawlJobBuilder(CrawlEventDispatch dispatch) {
        crawlEventDispatchFactory = Mockito.mock();
        when(crawlEventDispatchFactory.buildFor(any())).thenReturn(dispatch);
    }

    public TestCrawlJobBuilder(CrawlEventDispatchFactory factory) {
        this.crawlEventDispatchFactory = factory;
    }

    public CrawlJob buildForSeeds(List<URI> seeds) {
        CrawlJobFactory crawlJobFactory = TestCrawlJobFactoryBuilder
                .builder()
                .withCrawlEventDispatchFactory(crawlEventDispatchFactory)
                .build();
        CrawlJobConfiguration conf = buildTestConfigurationForSeeds(seeds);
        return crawlJobFactory.build(conf);
    }


    public static CrawlJobConfiguration buildTestConfigurationForSeeds(List<URI> seeds, RobotsTxt robotsTxt) {
        URI origin = extractOrigin(seeds.get(0));


        return CrawlJobConfiguration
                .newConfiguration(TestWebsiteCrawlFactory.TEST_OWNER, origin)
                .withTriggerForScheduledScanOn(EXPECTED_WORKSPACES_FOR_TRIGGER)
                .withSeeds(seeds)
                .withConcurrentConnections(seeds.size())
                .withRobotsTxt(robotsTxt)
                .build();
    }

    private static CrawlJobConfiguration buildTestConfigurationForSeeds(List<URI> seeds) {

        RobotsTxtAggregation robotsTxtAggregation = new RobotsTxtAggregation(new HTTPClient());

        RobotsTxt merged = robotsTxtAggregation.mergeConfigurations(
                seeds.stream().map(uri -> {
                    Workspace workspace = new Workspace();
                    workspace.setWebsiteUrl(uri.toString());
                    return workspace;
                }).toList()
        );

        return buildTestConfigurationForSeeds(seeds, merged);
    }
}
