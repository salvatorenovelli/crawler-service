package com.myseotoolbox.crawler.spider;


import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.spider.configuration.CrawlJobConfiguration;
import com.myseotoolbox.crawler.spider.event.CrawlEventDispatch;
import com.myseotoolbox.crawler.spider.filter.robotstxt.EmptyRobotsTxt;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.URI.create;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CrawlJobTest {

    public static final URI TEST_ORIGIN = URI.create("http://domain1");
    @Mock private WebPageReader pageReader;
    @Mock private CrawlEventDispatch dispatch;

    @Before
    public void setUp() throws Exception {
        when(pageReader.snapshotPage(any())).thenAnswer(arguments -> CrawlResult.forSnapshot(PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri(arguments.getArguments()[0].toString())));
    }

    @Test
    public void shouldFilterOutSeedsFromOutsideOrigin() {
        CrawlJob sut = initSut().withSeeds("http://domain1", "http://domain2").build();
        sut.start();
        Mockito.verify(dispatch).onPageCrawled(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void shouldNotifySubscribers() {
        CrawlJob sut = initSut().build();
        sut.start();
        Mockito.verify(dispatch).onPageCrawled(argThat(argument -> argument.getUri().equals("http://domain1")));
    }

    @Test
    public void shouldOnlyVisitSeedsNotTheRoot() {
        CrawlJob sut = initSut().withSeeds("http://domain1/path1", "http://domain1/path2").build();
        sut.start();
        Mockito.verify(dispatch).onPageCrawled(argThat(argument -> argument.getUri().equals("http://domain1/path1")));
        Mockito.verify(dispatch).onPageCrawled(argThat(argument -> argument.getUri().equals("http://domain1/path2")));
        verify(dispatch, atMost(2)).onPageCrawled(any());

    }

    @Test
    public void shouldNotVisitDuplicateSeeds() throws SnapshotException {
        CrawlJob sut = initSut().withSeeds("http://domain1", "http://domain1").build();
        sut.start();
        verify(pageReader).snapshotPage(create("http://domain1"));
        verifyNoMoreInteractions(pageReader);
    }

    @Test
    public void shouldNotifyForCrawlStarted() {
        String[] seeds = {"http://domain1/a", "http://domain1/b"};
        CrawlJob sut = initSut().withSeeds(seeds).build();
        sut.start();
        verify(dispatch).onCrawlStarted();
    }


    private CrawlJobBuilder initSut() {
        return new CrawlJobBuilder();
    }

    private class CrawlJobBuilder {
        private List<URI> seeds = Arrays.asList(create("http://domain1"));

        public CrawlJobBuilder withSeeds(String... seeds) {
            this.seeds = Arrays.stream(seeds).map(URI::create).collect(Collectors.toList());
            return this;
        }

        public CrawlJob build() {

            CrawlJobConfiguration conf = CrawlJobConfiguration
                    .newConfiguration("CrawlJobTest", TEST_ORIGIN)
                    .withRobotsTxt(new EmptyRobotsTxt(TEST_ORIGIN))
                    .withTriggerForUserInitiatedCrawlWorkspace(0)
                    .withSeeds(seeds).build();


            CrawlJobFactory crawlJobFactory = TestCrawlJobFactoryBuilder.builder()
                    .withWebPageReader(pageReader)
                    .withCrawlEventDispatch(dispatch)
                    .build();

            return crawlJobFactory.build(conf);

        }
    }
}