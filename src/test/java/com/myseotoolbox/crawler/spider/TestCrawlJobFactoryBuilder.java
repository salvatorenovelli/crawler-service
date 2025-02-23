package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.CrawlEventDispatchFactory;
import com.myseotoolbox.crawler.config.CrawlersPoolFactory;
import com.myseotoolbox.crawler.config.WebPageReaderFactory;
import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.spider.configuration.ClockUtils;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.ratelimiter.TestClockUtils;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReaderFactory;
import com.myseotoolbox.crawler.spider.sitemap.SitemapRepository;
import com.myseotoolbox.crawler.spider.sitemap.SitemapService;
import com.myseotoolbox.crawler.utils.CurrentThreadCrawlExecutorFactory;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class TestCrawlJobFactoryBuilder {
    private CrawlExecutorFactory testExecutorFactory = new CurrentThreadCrawlExecutorFactory();
    private HttpURLConnectionFactory connectionFactory = new HttpURLConnectionFactory();
    private WebsiteUriFilterFactory websiteUriFilterFactory = new WebsiteUriFilterFactory();
    private HttpRequestFactory requestFactory = new HttpRequestFactory(connectionFactory);
    private SitemapReaderFactory sitemapReaderFactory = new SitemapReaderFactory(requestFactory);
    private SitemapRepository sitemapRepository = Mockito.mock(SitemapRepository.class);
    private SitemapService sitemapService = new SitemapService(sitemapReaderFactory, sitemapRepository);
    private CrawlEventDispatchFactory crawlEventDispatchFactory;
    private ClockUtils clockUtils = new TestClockUtils();
    private WebPageReaderFactory webPageReaderReaderFactory;

    public static TestCrawlJobFactoryBuilder builder() {
        return new TestCrawlJobFactoryBuilder();
    }

    public TestCrawlJobFactoryBuilder withCrawlEventDispatch(CrawlEventDispatch crawlEventDispatch) {
        CrawlEventDispatchFactory mockCrawlEventDispatchFactory = Mockito.mock(CrawlEventDispatchFactory.class);
        when(mockCrawlEventDispatchFactory.buildFor(Mockito.any())).thenReturn(crawlEventDispatch);
        crawlEventDispatchFactory = mockCrawlEventDispatchFactory;
        return this;
    }

    public TestCrawlJobFactoryBuilder withCrawlEventDispatchFactory(CrawlEventDispatchFactory factory) {
        crawlEventDispatchFactory = factory;
        return this;
    }

    public TestCrawlJobFactoryBuilder withCLockUtils(ClockUtils clockUtils) {
        this.clockUtils = clockUtils;
        return this;
    }


    public TestCrawlJobFactoryBuilder withWebPageReaderFactory(WebPageReaderFactory webPageReaderFactory) {
        this.webPageReaderReaderFactory = webPageReaderFactory;
        return this;
    }

    public TestCrawlJobFactoryBuilder withFilterFactory(WebsiteUriFilterFactory websiteUriFilterFactory) {
        this.websiteUriFilterFactory = websiteUriFilterFactory;
        return this;
    }

    public TestCrawlJobFactoryBuilder withSitemapService(SitemapService sitemapService) {
        this.sitemapService = sitemapService;
        return this;
    }

    public TestCrawlJobFactoryBuilder withSitemapRepository(SitemapRepository sitemapRepository) {
        this.sitemapRepository = sitemapRepository;
        this.sitemapService = new SitemapService(sitemapReaderFactory, sitemapRepository);
        return this;
    }

    public TestCrawlJobFactoryBuilder withWebPageReader(WebPageReader pageReader) {
        WebPageReaderFactory mockWebPageReaderFactory = Mockito.mock();
        when(mockWebPageReaderFactory.build(any(), anyLong())).thenReturn(pageReader);
        this.withWebPageReaderFactory(mockWebPageReaderFactory);
        return this;
    }

    public CrawlJobFactory build() {
        if (webPageReaderReaderFactory == null) {
            webPageReaderReaderFactory = new WebPageReaderFactory(requestFactory, clockUtils);
        }

        if (crawlEventDispatchFactory == null) {
            CrawlEventDispatchFactory mockCrawlEventDispatchFactory = Mockito.mock(CrawlEventDispatchFactory.class);
            when(mockCrawlEventDispatchFactory.buildFor(Mockito.any())).thenReturn(Mockito.mock());
            crawlEventDispatchFactory = mockCrawlEventDispatchFactory;
        }


        return new CrawlJobFactory(webPageReaderReaderFactory,
                websiteUriFilterFactory, testExecutorFactory, crawlEventDispatchFactory,
                new CrawlersPoolFactory(), sitemapService);
    }
}
