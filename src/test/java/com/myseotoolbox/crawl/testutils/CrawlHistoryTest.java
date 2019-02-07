package com.myseotoolbox.crawl.testutils;


import com.myseotoolbox.crawl.model.PageCrawl;
import com.myseotoolbox.crawl.model.PageSnapshot;

public interface CrawlHistoryTest {

    void setValues(PageCrawl prevCrawl, PageSnapshot prevVal, PageSnapshot curVal);

}
