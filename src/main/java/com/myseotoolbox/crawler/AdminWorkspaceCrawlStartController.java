package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.CrawlWorkspaceRequest;
import com.myseotoolbox.crawler.model.CrawlWorkspaceResponse;
import com.myseotoolbox.crawler.model.EntityNotFoundException;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.filter.robotstxt.DefaultRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.IgnoredRobotsTxt;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import org.jsoup.helper.Validate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AdminWorkspaceCrawlStartController {

    private final CrawlJobFactory factory;
    private final WorkspaceRepository repository;
    private final WorkspaceCrawler workspaceCrawler;
    private final CrawlEventDispatchFactory crawlEventDispatchFactory;
    private final HTTPClient client;


    @GetMapping("/crawl-origin")
    public String crawlOrigin(@RequestParam("seeds") List<String> seeds, @RequestParam(value = "numConnections", defaultValue = "1") int numConnections) throws IOException {
        URI origin = WebsiteOriginUtils.extractOrigin(URI.create(seeds.get(0)));
        List<URI> seedsAsUri = seeds.stream().map(URI::create).collect(Collectors.toList());


        CrawlJobConfiguration configuration = getConfiguration("admin@myseotoolbox.com", origin, seedsAsUri, numConnections, true, 0L);

        CrawlJob job = factory.build(configuration, getCrawlEventsListener(configuration.getWebsiteCrawl()));
        job.start();
        return "Crawling " + seeds + " with " + numConnections + " parallel connections. Started on " + new Date();
    }

    @PostMapping("/crawl-workspace")
    public CrawlWorkspaceResponse crawlWorkspace(@RequestBody CrawlWorkspaceRequest request) throws EntityNotFoundException {
        Workspace ws = repository.findTopBySeqNumber(request.getWorkspaceNumber()).orElseThrow(EntityNotFoundException::new);
        URI origin = URI.create(ws.getWebsiteUrl());

        CrawlJobConfiguration conf = getConfiguration(request.getCrawlOwner(), origin, Collections.singletonList(origin), request.getNumConnections(), shouldIgnoreRobotsTxt(ws), ws.getCrawlerSettings().getCrawlDelayMillis());
        CrawlJob job = factory.build(conf, getCrawlEventsListener(conf.getWebsiteCrawl()));

        job.start();
        return new CrawlWorkspaceResponse(conf.getWebsiteCrawl().getId());
    }

    @GetMapping("/crawl-all-workspaces")
    public String crawlAllWorkspaces() {
        workspaceCrawler.crawlAllWorkspaces();
        return "Started on " + new Date() + "\n";
    }

    private CrawlJobConfiguration getConfiguration(String owner, URI origin, List<URI> seeds, int numConnections, boolean ignoreRobots, Long crawlDelayMillis) {
        CrawlJobConfiguration.Builder builder = CrawlJobConfiguration
                .newConfiguration(owner, origin)
                .withConcurrentConnections(numConnections)
                .withCrawlDelayMillis(crawlDelayMillis)
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

    private CrawlEventDispatch getCrawlEventsListener(WebsiteCrawl crawl) {
        return crawlEventDispatchFactory.get(crawl);
    }
}
