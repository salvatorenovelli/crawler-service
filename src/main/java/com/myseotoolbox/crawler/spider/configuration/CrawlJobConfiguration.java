package com.myseotoolbox.crawler.spider.configuration;

import com.myseotoolbox.crawler.spider.filter.robotstxt.RobotsTxt;
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


@Getter
@ToString
public class CrawlJobConfiguration {


    private final URI origin;
    private final Collection<URI> seeds;
    private final int maxConcurrentConnections;
    private final int crawledPageLimit;
    private final RobotsTxt robotsTxt;
    private final long minDelayMillis;

    private CrawlJobConfiguration(URI origin, Collection<URI> seeds, int maxConcurrentConnections, long minDelayMillis, int crawledPageLimit, RobotsTxt robotsTxt) {
        this.origin = origin;
        this.seeds = seeds;
        this.maxConcurrentConnections = ensureRange(maxConcurrentConnections, MIN_CONCURRENT_CONNECTIONS, MAX_CONCURRENT_CONNECTIONS);
        this.minDelayMillis = minDelayMillis;
        this.crawledPageLimit = crawledPageLimit;
        this.robotsTxt = robotsTxt;
    }

    public static Builder newConfiguration(URI origin) {
        return new Builder(origin);
    }

    public Collection<URI> getSeeds() {
        return Collections.unmodifiableCollection(seeds);
    }

    public List<String> getAllowedPaths() {
        return Collections.unmodifiableList(extractAllowedPathFromSeeds(seeds));
    }

    public long minDelayMillis() {
        return minDelayMillis;
    }

    public static class Builder {

        private Collection<URI> seeds = Collections.emptyList();
        private final URI origin;
        private int crawledPageLimit = DEFAULT_MAX_URL_PER_CRAWL;
        private int maxConcurrentConnections = DEFAULT_CONCURRENT_CONNECTIONS;
        private long minDelayMillis = MIN_CRAWL_DELAY_MILLIS;
        private RobotsTxt robotsTxt;

        public Builder(URI origin) {
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

        public Builder withMinDelayMillis(long minDelayMillis) {
            this.minDelayMillis = minDelayMillis;
            return this;
        }

        public CrawlJobConfiguration build() {
            Validate.notNull(robotsTxt, "robots.txt configuration missing. Please use defaultRobotsTxt() or configure it with withRobotsTxt(...)");
            return new CrawlJobConfiguration(origin, seeds, maxConcurrentConnections, minDelayMillis, crawledPageLimit, robotsTxt);
        }

        public Builder withMaxPagesCrawledLimit(int crawledPageLimit) {
            this.crawledPageLimit = crawledPageLimit;
            return this;
        }
    }

}
