package com.myseotoolbox.crawler.testutils;


import com.myseotoolbox.crawler.model.PageCrawl;
import com.myseotoolbox.crawler.model.PageSnapshot;

public interface CrawlHistoryTest {

    void setValues(PageCrawl prevCrawl, PageSnapshot prevVal, PageSnapshot curVal);

}
