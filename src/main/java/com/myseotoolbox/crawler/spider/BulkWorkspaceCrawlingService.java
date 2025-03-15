package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.CrawlDelayExpired;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.utils.WebsiteOriginUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.utils.FunctionalExceptionUtils.runOrLogWarning;

@Slf4j
@Component
public class BulkWorkspaceCrawlingService {

    public static final String CRAWL_OWNER = "cron@myseotoolbox.com";
    private final WorkspaceRepository workspaceRepository;
    private final CrawlJobFactory crawlJobFactory;
    private final CrawlDelayExpired crawlDelayExpired;
    private final Executor executor;
    private final RobotsTxtAggregation robotsTxtAggregation;

    public BulkWorkspaceCrawlingService(WorkspaceRepository workspaceRepository,
                                        CrawlJobFactory crawlJobFactory,
                                        CrawlDelayExpired crawlDelayExpired,
                                        RobotsTxtAggregation robotsTxtAggregation,
                                        @Qualifier("crawl-job-init-executor") Executor executor) {
        this.workspaceRepository = workspaceRepository;
        this.crawlJobFactory = crawlJobFactory;
        this.crawlDelayExpired = crawlDelayExpired;
        this.executor = executor;
        this.robotsTxtAggregation = robotsTxtAggregation;
    }


    public void crawlAllWorkspaces() {

        log.info("Starting workspaces crawl...");

        Map<URI, Set<Workspace>> workspacesByOrigin = groupWorkspacesByOrigin();

        log.info("Crawling {} hosts", workspacesByOrigin.size());

        workspacesByOrigin.forEach((origin, workspaces) ->
                executor.execute(() ->
                        runOrLogWarning(() -> {
                            Set<URI> seeds = extractSeeds(workspaces);

                            log.info("Starting crawl for {} with seeds: {}", origin, seeds);

                            RobotsTxt mergedConfiguration = robotsTxtAggregation.mergeConfigurations(workspaces);

                            CrawlJobConfiguration conf = CrawlJobConfiguration
                                    .newConfiguration(CRAWL_OWNER, origin)
                                    .withTriggerForScheduledScanOn(workspaces.stream().map(Workspace::getSeqNumber).collect(Collectors.toList()))
                                    .withSeeds(seeds)
                                    .withConcurrentConnections(seeds.size())
                                    .withRobotsTxt(mergedConfiguration)
                                    .withMaxPagesCrawledLimit(getHigherPageCrawledLimit(workspaces))
                                    .withCrawlDelayMillis(getHigherCrawlDelayMillis(workspaces))
                                    .build();


                            CrawlJob job = crawlJobFactory.make(conf);
                            job.run();
                        }, "Error while starting crawl for: " + origin))
        );

    }


    private long getHigherCrawlDelayMillis(Collection<Workspace> workspaces) {
        return workspaces.stream().mapToLong(workspace -> workspace.getCrawlerSettings().getCrawlDelayMillis()).max().orElse(0);
    }

    private int getHigherPageCrawledLimit(Collection<Workspace> workspaces) {
        return workspaces.stream().mapToInt(workspace -> workspace.getCrawlerSettings().getCrawledPageLimit()).max().orElse(0);
    }

    private Map<URI, Set<Workspace>> groupWorkspacesByOrigin() {
        return workspaceRepository.findAll()
                .stream()
                .filter(this::validOrigin)
                .filter(this::shouldCrawl)
                .collect(Collectors.groupingBy(this::extractOrigin, Collectors.toSet()));
    }

    private Set<URI> extractSeeds(Set<Workspace> workspaces) {
        return workspaces.stream().map(Workspace::getWebsiteUrl).map(URI::create).collect(Collectors.toSet());
    }

    private URI extractOrigin(Workspace workspace) {
        return WebsiteOriginUtils.extractOrigin(URI.create(workspace.getWebsiteUrl()));
    }

    private boolean shouldCrawl(Workspace workspace) {
        CrawlerSettings crawlerSettings = workspace.getCrawlerSettings();
        return crawlerSettings != null && crawlerSettings.isCrawlEnabled() && crawlDelayExpired.isCrawlDelayExpired(workspace);
    }

    private boolean validOrigin(Workspace workspace) {
        return WebsiteOriginUtils.isValidOrigin(workspace.getWebsiteUrl());
    }
}