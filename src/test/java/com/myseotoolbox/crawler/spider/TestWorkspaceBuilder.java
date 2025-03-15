package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WebsiteCrawlRepository;
import com.myseotoolbox.crawler.spider.configuration.CrawlerSettings;
import com.myseotoolbox.crawler.spider.configuration.FilterConfiguration;
import com.myseotoolbox.crawler.websitecrawl.CrawlTrigger;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.myseotoolbox.crawler.spider.CrawlerSettingsBuilder.from;
import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.DEFAULT_MAX_URL_PER_CRAWL;
import static org.mockito.Mockito.when;

public class TestWorkspaceBuilder {

    private final Workspace curWorkspace;
    private final FilterConfiguration filterConf = new FilterConfiguration(false);
    private final List<Workspace> allWorkspaces;
    private final WebsiteCrawlRepository crawlRepository;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private WebsiteCrawl lastWebsiteCrawl;

    public TestWorkspaceBuilder(List<Workspace> workspacesOut) {
        this(workspacesOut, null);
    }

    public TestWorkspaceBuilder(List<Workspace> workspacesOut, WebsiteCrawlRepository crawlRepository) {
        this.allWorkspaces = workspacesOut;
        this.curWorkspace = new Workspace();
        this.curWorkspace.setCrawlerSettings(CrawlerSettingsBuilder.defaultSettings().build());
        this.curWorkspace.setSeqNumber(counter.getAndIncrement());
        this.crawlRepository = crawlRepository;
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

        if (crawlRepository != null) {
            when(crawlRepository.findLatestByWorkspace(curWorkspace.getSeqNumber())).thenReturn(Optional.ofNullable(lastWebsiteCrawl));
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
        lastWebsiteCrawl = WebsiteCrawl.builder()
                .startedAt(Instant.now().plus(dayOffset, ChronoUnit.DAYS))
                .trigger(CrawlTrigger.forUserInitiatedWorkspaceCrawl(curWorkspace.getSeqNumber()))
                .build();
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
