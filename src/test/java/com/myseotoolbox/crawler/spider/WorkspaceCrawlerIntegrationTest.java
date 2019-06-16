package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.PageCrawlListener;
import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpURLConnectionFactory;
import com.myseotoolbox.crawler.repository.WebsiteCrawlLogRepository;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.configuration.RobotsTxtAggregation;
import com.myseotoolbox.crawler.spider.sitemap.SitemapReader;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@RunWith(MockitoJUnitRunner.class)
public class WorkspaceCrawlerIntegrationTest {


    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    private CrawlExecutorFactory testExecutorBuilder = new CurrentThreadCrawlExecutorFactory();
    private WebPageReaderFactory webPageReaderFactory = new WebPageReaderFactory(new HttpRequestFactory(new HttpURLConnectionFactory()));

    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private SitemapReader sitemapReader;
    @Mock private WebsiteCrawlLogRepository websiteCrawlLogRepository;
    @Mock private PageCrawlListener listener;
    @Mock private RobotsTxtAggregation robotsAggregation;
    private Executor executor = new CurrentThreadTestExecutorService();

    @Test
    public void name() {

        CrawlJobFactory crawlJobFactory = new CrawlJobFactory(webPageReaderFactory, new WebsiteUriFilterFactory(), testExecutorBuilder, sitemapReader);
        WorkspaceCrawler sut = new WorkspaceCrawler(workspaceRepository, crawlJobFactory, websiteCrawlLogRepository, listener, robotsAggregation, executor);

//        givenAWebsite()


    }

    private class CurrentThreadCrawlExecutorFactory extends CrawlExecutorFactory {
        @Override
        public ThreadPoolExecutor buildExecutor(String namePostfix, int concurrentConnections) {
            return new CurrentThreadTestExecutorService();
        }
    }

    private URI testUri(String url) {
        return testWebsiteBuilder.buildTestUri(url);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }
}
