package com.myseotoolbox.crawler.config;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.CrawlersPool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

@Component
@RequiredArgsConstructor
public class CrawlersPoolFactory {
    public CrawlersPool create(WebPageReader webPageReader, ThreadPoolExecutor executor) {
        return new CrawlersPool(webPageReader, executor);
    }
}
