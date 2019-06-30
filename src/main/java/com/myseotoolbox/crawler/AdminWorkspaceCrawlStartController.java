package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.EntityNotFoundException;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Profile("dev")
public class AdminWorkspaceCrawlStartController {

    private final CrawlJobFactory factory;
    private final WorkspaceRepository repository;
    private final WorkspaceCrawler workspaceCrawler;
    private final CrawlEventsListenerFactory crawlEventsListenerFactory;
    private final HTTPClient client;

    public AdminWorkspaceCrawlStartController(CrawlJobFactory factory, WorkspaceRepository repository, WorkspaceCrawler workspaceCrawler, CrawlEventsListenerFactory crawlEventsListenerFactory, HTTPClient client) {
        this.factory = factory;
        this.repository = repository;
        this.workspaceCrawler = workspaceCrawler;
        this.crawlEventsListenerFactory = crawlEventsListenerFactory;
        this.client = client;
    }

    @GetMapping("/scan-origin")
    public String scanOrigin(@RequestParam("seeds") List<String> seeds, @RequestParam(value = "numConnections", defaultValue = "1") int numConnections) throws IOException {
        URI origin = WebsiteOriginUtils.extractOrigin(URI.create(seeds.get(0)));
        List<URI> seedsAsUri = seeds.stream().map(URI::create).collect(Collectors.toList());


        CrawlJobConfiguration configuration = getConfiguration(origin, seedsAsUri, numConnections, true);


        CrawlJob job = factory.build(configuration, getCrawlEventsListener());
        job.start();
        return "Crawling " + seeds + " with " + numConnections + " parallel connections. Started on " + new Date();
    }

    @GetMapping("/scan-workspace")
    public String scanWorkspace(@RequestParam("seqNumber") int seqNumber, @RequestParam(value = "numConnections", defaultValue = "1") int numConnections) throws EntityNotFoundException, IOException {
        Workspace ws = repository.findTopBySeqNumber(seqNumber).orElseThrow(EntityNotFoundException::new);
        URI origin = URI.create(ws.getWebsiteUrl());

        CrawlJobConfiguration conf = getConfiguration(origin, Collections.singletonList(origin), numConnections, shouldIgnoreRobotsTxt(ws));
        CrawlJob job = factory.build(conf, getCrawlEventsListener());

        job.start();
        return "Crawling " + ws.getWebsiteUrl() + " with " + numConnections + " parallel connections. Started on " + new Date();
    }

    private CrawlJobConfiguration getConfiguration(URI origin, List<URI> seeds, int numConnections, boolean ignoreRobots) throws IOException {
        CrawlJobConfiguration.Builder builder = CrawlJobConfiguration
                .newConfiguration(origin)
                .withConcurrentConnections(numConnections)
                .withSeeds(seeds);

        if (ignoreRobots) {
            builder.withRobotsTxt(EmptyRobotsTxt.instance());
        } else {
            builder.withDefaultRobotsTxt(client);
        }

        return builder.build();
    }

    @GetMapping("/scan-all-workspaces")
    public String scanAllWorkspaces() {
        workspaceCrawler.crawlAllWorkspaces();
        return "Started on " + new Date();
    }


    private boolean shouldIgnoreRobotsTxt(Workspace ws) {
        CrawlerSettings crawlerSettings = ws.getCrawlerSettings();
        return crawlerSettings != null && crawlerSettings.getFilterConfiguration() != null &&
                crawlerSettings.getFilterConfiguration().shouldIgnoreRobotsTxt();
    }

    private CrawlEventListener getCrawlEventsListener() {
        return crawlEventsListenerFactory.getPageCrawlListener(new ObjectId());
    }
}
