package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.spider.event.PageCrawledEvent;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsIterableContaining;
import org.mockito.ArgumentMatcher;

import java.net.URI;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.argThat;


public class PageCrawledEventMatcherBuilder {
    private Matcher<String> crawlId = Matchers.any(String.class);
    private Matcher<String> pageSnapshotUri = Matchers.any(String.class);
    private Matcher<Iterable<? super URI>> sitemapInboundLinks = (Matcher<Iterable<? super URI>>) (Matcher) Matchers.any(Iterable.class);


    public static PageCrawledEventMatcherBuilder aPageCrawledEvent() {
        return new PageCrawledEventMatcherBuilder();
    }

    public PageCrawledEventMatcherBuilder withPageSnapshotUri(String uri) {
        this.pageSnapshotUri = equalTo(uri);
        return this;
    }

    public PageCrawledEventMatcherBuilder forCrawlId(String websiteCrawlId) {
        this.crawlId = equalTo(websiteCrawlId);
        return this;
    }

    public PageCrawledEventMatcherBuilder withSitemapInboundLinks(String testUri) {
        this.sitemapInboundLinks = IsIterableContaining.hasItem(URI.create(testUri));
        return this;
    }

    public PageCrawledEvent build() {
        return argThat(new ArgumentMatcher<PageCrawledEvent>() {
            @Override
            public boolean matches(PageCrawledEvent argument) {
                return crawlId.matches(argument.websiteCrawl().getId().toHexString()) &&
                        pageSnapshotUri.matches(argument.crawlResult().getPageSnapshot().getUri()) &&
                        sitemapInboundLinks.matches(argument.sitemapInboundLinks());
            }

            @Override
            public String toString() {
                return "PageCrawledEvent{" +
                        "websiteCrawlId=" + crawlId +
                        ", uri=" + pageSnapshotUri +
                        ", sitemapInboundLinks=" + sitemapInboundLinks +
                        '}';
            }
        });
    }
}
