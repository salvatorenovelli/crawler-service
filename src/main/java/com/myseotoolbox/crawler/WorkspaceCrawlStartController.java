package com.myseotoolbox.crawler;


import com.myseotoolbox.crawler.model.EntityNotFoundException;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Profile("dev")
public class WorkspaceCrawlStartController {

    private final CrawlJobFactory factory;
    private final WorkspaceRepository repository;

    public WorkspaceCrawlStartController(CrawlJobFactory factory, WorkspaceRepository repository) {
        this.factory = factory;
        this.repository = repository;
    }

    @GetMapping("/scan-origin")
    public String scanOrigin(@RequestParam("seeds") List<String> seeds, @RequestParam(value = "numConnections", defaultValue = "3") int numConnections) {
        CrawlJob job = factory.build(WebsiteOriginUtils.extractRoot(URI.create(seeds.get(0))), seeds.stream().map(URI::create).collect(Collectors.toList()), numConnections, WorkspaceCrawler.MAX_URL_PER_DOMAIN);
        job.start();
        return "Crawling " + seeds + " with " + numConnections + " parallel connections. Started on " + new Date();
    }

    @GetMapping("/scan-workspace")
    public String scanWorkspace(@RequestParam("seqNumber") int seqNumber, @RequestParam(value = "numConnections", defaultValue = "3") int numConnections) throws EntityNotFoundException {
        Workspace ws = repository.findTopBySeqNumber(seqNumber).orElseThrow(EntityNotFoundException::new);
        CrawlJob job = factory.build(URI.create(ws.getWebsiteUrl()), Collections.emptyList(), numConnections, WorkspaceCrawler.MAX_URL_PER_DOMAIN);
        job.start();
        return "Crawling " + ws.getWebsiteUrl() + " with " + numConnections + " parallel connections. Started on " + new Date();
    }
}
