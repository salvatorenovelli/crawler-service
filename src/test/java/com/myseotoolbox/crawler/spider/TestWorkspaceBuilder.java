package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.configuration.FilterConfiguration;
import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.myseotoolbox.crawler.spider.CrawlerSettingsBuilder.*;
import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.DEFAULT_MAX_URL_PER_CRAWL;
import static org.mockito.Mockito.when;

public class TestWorkspaceBuilder {

    private final Workspace curWorkspace;
    private final FilterConfiguration filterConf = new FilterConfiguration(false);
    private final List<Workspace> allWorkspaces;
    private final List<WebsiteCrawlLog> crawlLogs = new ArrayList<>();
    private WebsiteCrawlLogRepository websiteCrawlLogRepository;

    public TestWorkspaceBuilder(List<Workspace> workspacesOut, @Nullable WebsiteCrawlLogRepository websiteCrawlLogRepository) {
        this.allWorkspaces = workspacesOut;
        this.websiteCrawlLogRepository = websiteCrawlLogRepository;
        this.curWorkspace = new Workspace();
        this.curWorkspace.setCrawlerSettings(CrawlerSettingsBuilder.defaultSettings().build());
    }

    public TestWorkspaceBuilder withWebsiteUrl(String s) {
        curWorkspace.setWebsiteUrl(s);
        return this;
    }

    public TestWorkspaceBuilder withSequenceNumber(int seqNumber) {
        curWorkspace.setSeqNumber(seqNumber);
        return this;
    }

    public void build() {
        allWorkspaces.add(curWorkspace);

        if (websiteCrawlLogRepository != null) {
            when(websiteCrawlLogRepository
                    .findTopByOriginOrderByDateDesc(curWorkspace.getWebsiteUrl()))
                    .thenAnswer(invocation -> crawlLogs.stream()
                            .filter(websiteCrawlLog -> websiteCrawlLog.getOrigin().equals(invocation.getArgument(0)))
                            .findFirst());
        }
    }

    public TestWorkspaceBuilder withCrawlingDisabled() {
        CrawlerSettings settings = from(curWorkspace.getCrawlerSettings())
                .withCrawlEnabled(false)
                .build();
        curWorkspace.setCrawlerSettings(settings);
        return this;
    }

    public TestWorkspaceBuilder withCrawlingIntervalOf(int days) {
        CrawlerSettings settings = from(curWorkspace.getCrawlerSettings())
                .withCrawlIntervalDays(days)
                .build();
        curWorkspace.setCrawlerSettings(settings);
        return this;
    }

    public TestWorkspaceBuilder withLastCrawlHappened(int dayOffset) {
        crawlLogs.add(new WebsiteCrawlLog(curWorkspace.getWebsiteUrl(), LocalDate.now().plusDays(dayOffset)));
        return this;
    }

    public TestWorkspaceBuilder withCrawlerSettings(CrawlerSettings settings) {
        curWorkspace.setCrawlerSettings(settings);
        return this;
    }


    public TestWorkspaceBuilder withCrawlDelayMillis(long crawlDelayMillis) {
        CrawlerSettings settings = from(curWorkspace.getCrawlerSettings())
                .withCrawlDelayMillis(crawlDelayMillis)
                .build();
        curWorkspace.setCrawlerSettings(settings);
        return this;
    }

    public class CrawlerSettingsInnerBuilder {

        private final TestWorkspaceBuilder testWorkspaceBuilder;
        private int crawlIntervalDays = 1;
        private long crawlDelayMillis = 0;
        private boolean ignoreRobotsTxt = false;
        private int maxConcurrentConnections = 1;
        private boolean crawlEnabled = true;

        public CrawlerSettingsInnerBuilder(TestWorkspaceBuilder testWorkspaceBuilder) {
            this.testWorkspaceBuilder = testWorkspaceBuilder;
        }

        public CrawlerSettingsInnerBuilder ignoringRobotsTxt(boolean b) {
            this.ignoreRobotsTxt = b;
            return this;
        }

        public TestWorkspaceBuilder and() {
            CrawlerSettings crawlerSettings = new CrawlerSettings(maxConcurrentConnections, crawlEnabled, crawlIntervalDays, crawlDelayMillis, new FilterConfiguration(ignoreRobotsTxt), DEFAULT_MAX_URL_PER_CRAWL);
            testWorkspaceBuilder.withCrawlerSettings(crawlerSettings);
            return testWorkspaceBuilder;
        }
    }
}
