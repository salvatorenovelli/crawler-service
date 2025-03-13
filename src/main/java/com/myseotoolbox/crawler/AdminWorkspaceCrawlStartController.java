package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.CrawlWorkspaceRequest;
import com.myseotoolbox.crawler.model.CrawlWorkspaceResponse;
import com.myseotoolbox.crawler.model.EntityNotFoundException;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.BulkWorkspaceCrawlingService;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.filter.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.IgnoredRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminWorkspaceCrawlStartController {

    private final CrawlJobFactory factory;
    private final BulkWorkspaceCrawlingService bulkWorkspaceCrawlingService;
    private final WorkspaceRepository repository;
    private final HTTPClient client;


    @PostMapping("/crawl-workspace")
    public CrawlWorkspaceResponse crawlWorkspace(@Valid @RequestBody CrawlWorkspaceRequest request) throws EntityNotFoundException {
        Workspace ws = getWorkspace(request);

        verifyCrawlerSettings(ws.getCrawlerSettings());

        URI origin = URI.create(ws.getWebsiteUrl());

        CrawlJobConfiguration conf = getConfiguration(request.getCrawlOwner(), ws.getSeqNumber(), origin, Collections.singletonList(origin), request.getNumConnections(), shouldIgnoreRobotsTxt(ws), ws.getCrawlerSettings().getCrawlDelayMillis());
        CrawlJob job = factory.make(conf);

        job.start();
        return new CrawlWorkspaceResponse(conf.getWebsiteCrawl().getId());
    }

    @GetMapping("/crawl-all-workspaces")
    public String crawlAllWorkspaces() {
        bulkWorkspaceCrawlingService.crawlAllWorkspaces();
        return "Started on " + new Date() + "\n";
    }

    private CrawlJobConfiguration getConfiguration(String owner, int workspaceNumber, URI origin, List<URI> seeds, int numConnections, boolean ignoreRobots, Long crawlDelayMillis) {
        CrawlJobConfiguration.Builder builder = CrawlJobConfiguration
                .newConfiguration(owner, origin)
                .withConcurrentConnections(numConnections)
                .withCrawlDelayMillis(crawlDelayMillis)
                .withTriggerForUserInitiatedCrawlWorkspace(workspaceNumber)
                .withSeeds(seeds);

        RobotsTxt robotsTxt = buildRobotsTxt(origin, ignoreRobots);

        builder.withRobotsTxt(robotsTxt);

        return builder.build();
    }

    private RobotsTxt buildRobotsTxt(URI origin, boolean ignoreRobots) {
        try {
            String content = client.get(origin.resolve("/robots.txt"));
            if (ignoreRobots) {
                return new IgnoredRobotsTxt(origin.toString(), content.getBytes());
            } else {
                return new DefaultRobotsTxt(origin.toString(), content.getBytes());
            }
        } catch (IOException e) {
            return new EmptyRobotsTxt(origin);
        }
    }

    private boolean shouldIgnoreRobotsTxt(Workspace ws) {
        CrawlerSettings crawlerSettings = ws.getCrawlerSettings();
        return crawlerSettings != null && crawlerSettings.getFilterConfiguration() != null &&
                crawlerSettings.getFilterConfiguration().shouldIgnoreRobotsTxt();
    }

    private Workspace getWorkspace(CrawlWorkspaceRequest request) throws EntityNotFoundException {
        try {
            return repository.findTopBySeqNumber(request.getWorkspaceNumber()).orElseThrow(EntityNotFoundException::new);
        } catch (EntityNotFoundException e) {
            log.warn("Workspace not found: {} - Request: {}", request.getWorkspaceNumber(), request);
            throw e;
        }
    }

    private void verifyCrawlerSettings(CrawlerSettings crawlerSettings) {
        if (crawlerSettings == null) throw new IllegalStateException("Crawler Settings can't be null");
    }
}
