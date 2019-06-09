package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlListener;
import com.myseotoolbox.crawler.model.CrawlerSettings;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.configuration.CrawlConfiguration;
import com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils;
import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@Component
public class WorkspaceCrawler {


    private final WorkspaceRepository workspaceRepository;
    private final CrawlJobFactory crawlJobFactory;
    private final WebsiteCrawlLogRepository websiteCrawlLogRepository;
    private final PageCrawlListener crawlListener;
    private final Executor executor;

    public WorkspaceCrawler(WorkspaceRepository workspaceRepository,
                            CrawlJobFactory crawlJobFactory,
                            WebsiteCrawlLogRepository websiteCrawlLogRepository,
                            PageCrawlListener crawlListener,
                            @Qualifier("crawl-job-init-executor") Executor executor) {
        this.workspaceRepository = workspaceRepository;
        this.crawlJobFactory = crawlJobFactory;
        this.websiteCrawlLogRepository = websiteCrawlLogRepository;
        this.crawlListener = crawlListener;
        this.executor = executor;
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
                executor.execute(() -> runOrLogWarning(() -> {
                    log.info("Crawling {} with seeds: {}", baseDomainPath, seeds);

                    CrawlConfiguration conf = CrawlConfiguration
                            .newConfiguration(baseDomainPath)
                            .withSeeds(new ArrayList<>(seeds))
                            .withConcurrentConnections(seeds.size())
                            .build();

                    CrawlJob job = crawlJobFactory.build(conf, crawlListener);
                    job.start();
                    //this needs to go
                    seeds.forEach(seed -> websiteCrawlLogRepository.save(new WebsiteCrawlLog(seed.toString(), LocalDate.now())));
                }, "Error while starting crawl for: " + baseDomainPath))
        );

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


    private String addTrailingSlashIfMissing(String uri) {
        return uri + (uri.endsWith("/") ? "" : "/");
    }
}
