package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Component
public class WorkspaceCrawler {

    public static final int MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN = 5;
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
                .filter(WebsiteOriginUtils::isValidOrigin)
                .map(this::addTrailingSlashIfMissing)
                .map(URI::create)
                .collect(Collectors.groupingBy(WebsiteOriginUtils::extractOrigin, Collectors.toSet()));

        seedsByOrigin.forEach((origin, seeds) ->
                runOrLogWarning(() -> {
                    CrawlJob job = crawlFactory.build(origin, new ArrayList<>(seeds), getNumConcurrentConnections(seeds));
                    job.start();
                }, "Error while starting crawl for: " + origin));

    }

    private int getNumConcurrentConnections(Set<URI> seeds) {
        return ensureRange(seeds.size(), 1, MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN);
    }

    private int ensureRange(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private String addTrailingSlashIfMissing(String uri) {
        return uri + (uri.endsWith("/") ? "" : "/");
    }
}
