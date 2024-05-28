package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.testutils.TestWebsiteCrawlFactory;
import org.bson.types.ObjectId;

import java.net.URI;
import java.util.Collections;

public class PageCrawledEventTestBuilder {


    private CrawlResult crawlResult;
    private WebsiteCrawl websiteCrawl;
    private ObjectId crawlID = new ObjectId();
    private PageSnapshot snapshot;
    private final String origin;

    public PageCrawledEventTestBuilder(String origin) {
        this.origin = origin;
    }

    public static PageCrawledEventTestBuilder aPageCrawledEvent(String origin) {
        return new PageCrawledEventTestBuilder(origin);
    }

    public PageCrawledEventTestBuilder withStandardValuesForPath(String path) {
        snapshot = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(URI.create(origin).resolve(path).toASCIIString());
        return this;
    }

    public PageCrawledEvent build() {
        this.crawlResult = CrawlResult.forSnapshot(snapshot);
        this.websiteCrawl = TestWebsiteCrawlFactory.newWebsiteCrawlFor(origin, Collections.emptyList());
        return new PageCrawledEvent(websiteCrawl, crawlResult);
    }

    public PageCrawledEventTestBuilder withCrawlId(String websiteCrawlId) {
        this.crawlID = new ObjectId(websiteCrawlId);
        return this;
    }

}
