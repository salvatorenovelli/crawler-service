package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.Validate;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.myseotoolbox.crawler.spider.configuration.AllowedPathFromSeeds.extractAllowedPathFromSeeds;
import static com.myseotoolbox.crawler.spider.configuration.DefaultCrawlerSettings.*;
import static com.myseotoolbox.crawler.utils.EnsureRange.ensureRange;
import static com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory.newWebsiteCrawlFor;


@Getter
@ToString
public class CrawlJobConfiguration {


    private final URI origin;
    private final Collection<URI> seeds;
    private final int maxConcurrentConnections;
    private final int crawledPageLimit;
    private final RobotsTxt robotsTxt;
    private final long crawlDelayMillis;
    private final WebsiteCrawl websiteCrawl;

    private CrawlJobConfiguration(String owner, URI origin, Collection<URI> seeds, int maxConcurrentConnections, long crawlDelayMillis, int crawledPageLimit, RobotsTxt robotsTxt) {
        this.origin = origin;
        this.seeds = seeds;
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.crawlDelayMillis = crawlDelayMillis;
        this.crawledPageLimit = crawledPageLimit;
        this.robotsTxt = robotsTxt;
        this.websiteCrawl = newWebsiteCrawlFor(owner, origin.toString(), seeds);
    }

    public static Builder newConfiguration(String owner, URI origin) {
        Validate.notNull(owner, "crawlOwner cannot be null");
        return new Builder(owner, origin);
    }

    public Collection<URI> getSeeds() {
        return Collections.unmodifiableCollection(seeds);
    }

    public List<String> getAllowedPaths() {
        return Collections.unmodifiableList(extractAllowedPathFromSeeds(seeds));
    }

    public long crawlDelayMillis() {
        return crawlDelayMillis;
    }

    public static class Builder {

        private Collection<URI> seeds = Collections.emptyList();
        private final URI origin;
        private final String owner;
        private int crawledPageLimit = DEFAULT_MAX_URL_PER_CRAWL;
        private int maxConcurrentConnections = DEFAULT_CONCURRENT_CONNECTIONS;
        private long crawlDelayMillis = MIN_CRAWL_DELAY_MILLIS;
        private RobotsTxt robotsTxt;

        public Builder(String owner, URI origin) {
            this.owner = owner;
            this.origin = origin;
        }

        public Builder withSeeds(Collection<URI> seeds) {
            this.seeds = Collections.unmodifiableCollection(seeds);
            return this;
        }

        public Builder withConcurrentConnections(int maxConcurrentConnections) {
            this.maxConcurrentConnections = maxConcurrentConnections;
            return this;
        }

        public Builder withRobotsTxt(RobotsTxt robotsTxt) {
            this.robotsTxt = robotsTxt;
            return this;
        }

        public Builder withCrawlDelayMillis(long crawlDelayMillis) {
            this.crawlDelayMillis = crawlDelayMillis;
            return this;
        }

        public CrawlJobConfiguration build() {
            Validate.notNull(robotsTxt, "robots.txt configuration missing. Please use defaultRobotsTxt() or configure it with withRobotsTxt(...)");
            return new CrawlJobConfiguration(owner, origin, seeds, maxConcurrentConnections, crawlDelayMillis, crawledPageLimit, robotsTxt);
        }

        public Builder withMaxPagesCrawledLimit(int crawledPageLimit) {
            this.crawledPageLimit = crawledPageLimit;
            return this;
        }
    }

}
