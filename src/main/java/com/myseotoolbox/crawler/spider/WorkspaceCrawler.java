package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractRoot;
import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;
import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Component
public class WorkspaceCrawler {

    public static final int MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN = 5;
    private final WorkspaceRepository workspaceRepository;
    private final CrawlJobFactory crawlJobFactory;
    private final WebsiteCrawlLogRepository websiteCrawlLogRepository;

    public WorkspaceCrawler(WorkspaceRepository workspaceRepository, CrawlJobFactory crawlJobFactory, WebsiteCrawlLogRepository websiteCrawlLogRepository) {
        this.workspaceRepository = workspaceRepository;
        this.crawlJobFactory = crawlJobFactory;
        this.websiteCrawlLogRepository = websiteCrawlLogRepository;
    }

    public void crawlAllWorkspaces() {

        Map<URI, Set<URI>> seedsByOrigin = workspaceRepository.findAll()
                .stream()
                .filter(this::shouldCrawl)
                .map(Workspace::getWebsiteUrl)
                .filter(WebsiteOriginUtils::isValidOrigin)
                .map(this::addTrailingSlashIfMissing)
                .map(URI::create)
                .collect(Collectors.groupingBy(WebsiteOriginUtils::extractRoot, Collectors.toSet()));

        seedsByOrigin.forEach((baseDomainPath, seeds) ->
                runOrLogWarning(() -> {
                    CrawlJob job = crawlJobFactory.build(baseDomainPath, new ArrayList<>(seeds), getNumConcurrentConnections(seeds));
                    job.start();
                    websiteCrawlLogRepository.save(new WebsiteCrawlLog(baseDomainPath.toString(), LocalDate.now()));
                }, "Error while starting crawl for: " + baseDomainPath));

    }

    private boolean shouldCrawl(Workspace workspace) {
        return workspace.getCrawlerSettings().isCrawlEnabled() && isDelayExpired(workspace);
    }

    private boolean isDelayExpired(Workspace workspace) {
        URI origin = extractRoot(URI.create(workspace.getWebsiteUrl()));

        return websiteCrawlLogRepository.findTopByOriginOrderByDateDesc(origin.toString()).map(lastCrawl -> {
            int crawlIntervalDays = workspace.getCrawlerSettings().getCrawlIntervalDays();
            return LocalDate.now().minusDays(crawlIntervalDays).compareTo(lastCrawl.getDate()) >= 0;
        }).orElse(true);
    }

    private int getNumConcurrentConnections(Set<URI> seeds) {
        return ensureRange(seeds.size(), 1, MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN);
    }

    private String addTrailingSlashIfMissing(String uri) {
        return uri + (uri.endsWith("/") ? "" : "/");
    }
}
