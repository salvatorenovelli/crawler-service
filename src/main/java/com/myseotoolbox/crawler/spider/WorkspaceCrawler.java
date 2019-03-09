package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkspaceCrawler {

    public static final int DEFAULT_NUM_CONNECTIONS = 3;
    private final WorkspaceRepository workspaceRepository;
    private final CrawlJobFactory crawlFactory;

    public WorkspaceCrawler(WorkspaceRepository workspaceRepository, CrawlJobFactory crawlFactory) {
        this.workspaceRepository = workspaceRepository;
        this.crawlFactory = crawlFactory;
    }

    public void crawlAllWorkspaces() {

        Map<URI, Set<URI>> seedsByOrigin = workspaceRepository.findAll()
                .stream()
                .map(Workspace::getWebsiteUrl)
                .map(URI::create)
                .collect(Collectors.groupingBy(WebsiteOriginUtils::extractOrigin, Collectors.toSet()));

        seedsByOrigin.forEach((origin, seeds) -> {
            CrawlJob job = crawlFactory.build(origin, new ArrayList<>(seeds), DEFAULT_NUM_CONNECTIONS);
            job.start();
        });


    }
}