package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.testutils.CurrentThreadTestExecutorService;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@RunWith(MockitoJUnitRunner.class)
public class SpiderIntegrationTest {

    private WebsiteUriFilterBuilder uriFilterBuilder = new WebsiteUriFilterBuilder();
    private ExecutorBuilder executorBuilder = new CurrentThreadExecutorBuilder();

    private InputStream robotsTxt = getClass().getResourceAsStream("/robots.txt");
    TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();
    @Mock private Consumer<PageSnapshot> crawledPagesListener;

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

        verify(crawledPagesListener).accept(uri("/source"));
        verify(crawledPagesListener).accept(uri("/abc"));
        verify(crawledPagesListener).accept(uri("/cde"));

        verifyNoMoreInteractions(crawledPagesListener);

    }

    @Test
    public void shouldNotVisitBlockedUri() {
        TestWebsite save = givenAWebsite()
                .withRobotsTxt(robotsTxt)
                .havingRootPage().redirectingTo(301, "/blocked-by-robots").save();

        CrawlJob job = buildForOrigin(testUri("/"));
        job.start();

        List<ReceivedRequest> receivedRequests = save.getRequestsReceived();

        assertThat(receivedRequests, hasSize(2));
        assertThat(receivedRequests.get(0).getUrl(), is("/robots.txt"));
        assertThat(receivedRequests.get(1).getUrl(), is("/"));

    }

    private PageSnapshot uri(String uri) {
        return argThat(argument -> argument.getUri().equals(testUri(uri).toString()));
    }

    private CrawlJob buildForOrigin(URI origin) {

        UriFilter uriFilter = uriFilterBuilder.buildForOrigin(origin);
        CrawlJob job = new CrawlJob(origin, Collections.emptyList(), new WebPageReader(uriFilter),
                uriFilter,
                executorBuilder.buildExecutor(origin.getHost(), 1));


        job.subscribeToPageCrawled(crawledPagesListener);
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
