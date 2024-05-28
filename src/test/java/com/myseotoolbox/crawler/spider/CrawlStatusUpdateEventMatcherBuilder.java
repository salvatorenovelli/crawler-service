package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.event.CrawlStatusUpdateEvent;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;

import static org.hamcrest.CoreMatchers.any;

public class CrawlStatusUpdateEventMatcherBuilder {
    private Matcher<Integer> visited = any(Integer.class);
    private Matcher<Integer> pending = any(Integer.class);
    private Matcher<String> crawlId = any(String.class);


    public static CrawlStatusUpdateEventMatcherBuilder aCrawlStatusUpdateEvent() {
        return new CrawlStatusUpdateEventMatcherBuilder();
    }

    public CrawlStatusUpdateEventMatcherBuilder withVisited(int visitedCount) {
        this.visited = Matchers.equalTo(visitedCount);
        return this;
    }

    public CrawlStatusUpdateEventMatcherBuilder withPending(int pendingCount) {
        this.pending = Matchers.equalTo(pendingCount);
        return this;
    }

    public CrawlStatusUpdateEventMatcherBuilder withCrawlId(String websiteCrawlId) {
        this.crawlId = Matchers.equalTo(websiteCrawlId);
        return this;
    }

    public CrawlStatusUpdateEvent build() {
        return ArgumentMatchers.argThat(new ArgumentMatcher<CrawlStatusUpdateEvent>() {
            @Override
            public boolean matches(CrawlStatusUpdateEvent argument) {
                return visited.matches(argument.getVisited()) &&
                        pending.matches(argument.getPending()) &&
                        crawlId.matches(argument.getWebsiteCrawl().getId().toHexString())

                        ;
            }

            @Override
            public String toString() {
                return "CrawlStatusUpdateEvent{" +
                        "crawlId=" + crawlId +
                        ", visited=" + visited +
                        ", pending=" + pending +
                        '}';
            }
        });

    }
}
