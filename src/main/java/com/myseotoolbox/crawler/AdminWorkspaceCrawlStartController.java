package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.EntityNotFoundException;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
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
    private final PageCrawlListener pageCrawlListener;
    private final HTTPClient client;

    public AdminWorkspaceCrawlStartController(CrawlJobFactory factory, WorkspaceRepository repository, WorkspaceCrawler workspaceCrawler, PageCrawlListener pageCrawlListener, HTTPClient client) {
        this.factory = factory;
        this.repository = repository;
        this.workspaceCrawler = workspaceCrawler;
        this.pageCrawlListener = pageCrawlListener;
        this.client = client;
    }

    @GetMapping("/scan-origin")
    public String scanOrigin(@RequestParam("seeds") List<String> seeds, @RequestParam(value = "numConnections", defaultValue = "1") int numConnections) throws IOException {
        URI origin = WebsiteOriginUtils.extractRoot(URI.create(seeds.get(0)));
        List<URI> seedsAsUri = seeds.stream().map(URI::create).collect(Collectors.toList());

        CrawlJobConfiguration build = CrawlJobConfiguration
                .newConfiguration(origin)
                .withSeeds(seedsAsUri)
                .withDefaultRobotsTxt(client)
                .withConcurrentConnections(numConnections)
                .build();

        CrawlJob job = factory.build(build, pageCrawlListener);
        job.start();
        return "Crawling " + seeds + " with " + numConnections + " parallel connections. Started on " + new Date();
    }

    @GetMapping("/scan-workspace")
    public String scanWorkspace(@RequestParam("seqNumber") int seqNumber, @RequestParam(value = "numConnections", defaultValue = "1") int numConnections) throws EntityNotFoundException, IOException {
        Workspace ws = repository.findTopBySeqNumber(seqNumber).orElseThrow(EntityNotFoundException::new);
        URI origin = URI.create(ws.getWebsiteUrl());

        CrawlJobConfiguration conf = CrawlJobConfiguration
                .newConfiguration(origin)
                .withSeeds(Collections.singletonList(origin))
                .withDefaultRobotsTxt(client)
                .withConcurrentConnections(numConnections)
                .build();

        CrawlJob job = factory.build(conf, pageCrawlListener);

        job.start();
        return "Crawling " + ws.getWebsiteUrl() + " with " + numConnections + " parallel connections. Started on " + new Date();
    }

    @GetMapping("/scan-all-workspaces")
    public String scanAllWorkspaces() {
        workspaceCrawler.crawlAllWorkspaces();
        return "Started on " + new Date();
    }


}
