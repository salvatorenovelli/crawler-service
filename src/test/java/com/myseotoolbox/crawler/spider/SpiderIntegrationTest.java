package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@RunWith(MockitoJUnitRunner.class)
public class SpiderIntegrationTest {

    private WebsiteUriFilterBuilder uriFilterBuilder = new WebsiteUriFilterBuilder();
    private ExecutorBuilder executorBuilder = new CurrentThreadExecutorBuilder();

    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    @Mock private Consumer<PageSnapshot> crawledPages;

    @Before
    public void setUp() throws Exception {
        testWebsiteBuilder.run();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }

    @Test
    public void basicLinkFollowing() {

        givenAWebsite()
                .havingPage("/source").withLinksTo("/abc", "/cde")
                .save();


        CrawlJob job = buildForOrigin(testUri("/source"));
        job.start();


        verify(crawledPages).accept(uri("/source"));
        verify(crawledPages).accept(uri("/abc"));
        verify(crawledPages).accept(uri("/cde"));

        verifyNoMoreInteractions(crawledPages);


    }

    private PageSnapshot uri(String uri) {
        return argThat(argument -> argument.getUri().equals(testUri(uri).toString()));
    }

    private CrawlJob buildForOrigin(URI origin) {

        CrawlJob job = new CrawlJob(origin, Collections.emptyList(), new WebPageReader(),
                uriFilterBuilder.buildForOrigin(origin),
                executorBuilder.buildExecutor(origin.getHost(), 1));


        job.subscribeToPageCrawled(crawledPages);
        return job;
    }

    private class CurrentThreadExecutorBuilder extends ExecutorBuilder {
        @Override
        public ExecutorService buildExecutor(String namePostfix, int concurrentConnections) {
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
