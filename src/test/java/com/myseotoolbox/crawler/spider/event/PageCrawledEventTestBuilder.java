package com.myseotoolbox.crawler.spider.event;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawlFactory;
import org.bson.types.ObjectId;

import java.net.URI;
import java.util.Collections;

public class PageCrawledEventTestBuilder {

    public static final ObjectId TEST_CRAWL_ID = new ObjectId();
    public static final String TEST_ORIGIN = "http://origin";

    private CrawlResult crawlResult;
    private WebsiteCrawl websiteCrawl;

    public PageCrawledEventTestBuilder withStandardValuesForPath(String path) {
        PageSnapshot snapshot = PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(URI.create(TEST_ORIGIN).resolve(path).toASCIIString());
        this.crawlResult = CrawlResult.forSnapshot(snapshot);
        this.websiteCrawl = WebsiteCrawlFactory.newWebsiteCrawlFor(TEST_CRAWL_ID, TEST_ORIGIN, Collections.emptyList());
        return this;
    }


    public PageCrawledEvent build() {
        return new PageCrawledEvent(websiteCrawl, crawlResult);
    }

    public static PageCrawledEventTestBuilder aTestPageCrawledEvent() {
        return new PageCrawledEventTestBuilder();
    }
}
