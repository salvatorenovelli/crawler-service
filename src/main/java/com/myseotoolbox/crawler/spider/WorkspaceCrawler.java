package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlerSettings;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class WorkspaceCrawler {

    public static final int MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN = 5;
    public static final int MAX_URL_PER_DOMAIN = 10000;
    private final WorkspaceRepository workspaceRepository;
    private final CrawlJobFactory crawlJobFactory;
    private final WebsiteCrawlLogRepository websiteCrawlLogRepository;

    public WorkspaceCrawler(WorkspaceRepository workspaceRepository, CrawlJobFactory crawlJobFactory, WebsiteCrawlLogRepository websiteCrawlLogRepository) {
        this.workspaceRepository = workspaceRepository;
        this.crawlJobFactory = crawlJobFactory;
        this.websiteCrawlLogRepository = websiteCrawlLogRepository;
    }

    public void crawlAllWorkspaces() {
        log.info("Starting workspaces crawl...");

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
                    log.info("Crawling {} with seeds: {}", baseDomainPath, seeds);
                    CrawlJob job = crawlJobFactory.build(baseDomainPath, new ArrayList<>(seeds), getNumConcurrentConnections(seeds), MAX_URL_PER_DOMAIN);
                    job.start();
                    seeds.forEach(seed -> websiteCrawlLogRepository.save(new WebsiteCrawlLog(seed.toString(), LocalDate.now())));
                }, "Error while starting crawl for: " + baseDomainPath));

    }

    private boolean shouldCrawl(Workspace workspace) {
        CrawlerSettings crawlerSettings = workspace.getCrawlerSettings();
        return crawlerSettings != null && crawlerSettings.isCrawlEnabled() && isDelayExpired(workspace);
    }

    private boolean isDelayExpired(Workspace workspace) {

        return websiteCrawlLogRepository.findTopByOriginOrderByDateDesc(workspace.getWebsiteUrl()).map(lastCrawl -> {
            int crawlIntervalDays = workspace.getCrawlerSettings().getCrawlIntervalDays();
            boolean delayExpired = LocalDate.now().minusDays(crawlIntervalDays).compareTo(lastCrawl.getDate()) >= 0;

            if (!delayExpired) {
                log.info("Workspace {} doesnt need crawl yet. Crawl interval: {} Last Crawl: {}",
                        workspace.getOwnerName() + " - " + workspace.getName(),
                        crawlIntervalDays,
                        lastCrawl.getDate());
            }

            return delayExpired;
        }).orElse(true);
    }

    private int getNumConcurrentConnections(Set<URI> seeds) {
        return ensureRange(seeds.size(), 1, MAX_CONCURRENT_CONNECTIONS_PER_DOMAIN);
    }

    private String addTrailingSlashIfMissing(String uri) {
        return uri + (uri.endsWith("/") ? "" : "/");
    }
}
