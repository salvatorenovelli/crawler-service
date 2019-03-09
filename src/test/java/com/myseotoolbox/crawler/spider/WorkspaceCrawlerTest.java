package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlerSettings;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.myseotoolbox.crawler.spider.WorkspaceCrawler.DEFAULT_NUM_CONNECTIONS;
import static java.net.URI.create;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceCrawlerTest {


    private final List<CrawlJob> mockJobs = new ArrayList<>();
    private final List<Workspace> allWorkspaces = new ArrayList<>();

    @Mock private CrawlJobFactory crawlFactory;
    @Mock private WorkspaceRepository workspaceRepository;

    WorkspaceCrawler sut;

    @Before
    public void setUp() throws Exception {
        sut = new WorkspaceCrawler(workspaceRepository, crawlFactory);

        when(crawlFactory.build(any(URI.class), anyList(), anyInt())).thenAnswer(
                invocation -> {
                    CrawlJob mock = mock(CrawlJob.class);
                    mockJobs.add(mock);
                    return mock;
                }
        );

        when(workspaceRepository.findAll()).thenReturn(allWorkspaces);
    }

    @Test
    public void shouldCrawlAllTheWorkspaces() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host2").build();

        sut.crawlAllWorkspaces();

        crawlStartedFor("http://host1");
        crawlStartedFor("http://host2");
    }


    @Test
    public void shouldNotCrawlTwice() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1").build();


        sut.crawlAllWorkspaces();

        crawlStartedFor("http://host1");
        verifyNoMoreCrawls();
    }


    @Test
    public void shouldGroupByOrigin() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2").build();
        givenAWorkspace().withWebsiteUrl("http://host2").build();

        sut.crawlAllWorkspaces();

        crawlStartedForOriginWithSeeds("http://host1", List.of("http://host1", "http://host1/path1", "http://host1/path2"));
        crawlStartedForOriginWithSeeds("http://host2", List.of("http://host2"));
    }


    @Test
    public void shouldNotHaveDuplicatesInSeeds() {
        givenAWorkspace().withWebsiteUrl("http://host1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path1").build();
        givenAWorkspace().withWebsiteUrl("http://host1/path2").build();

        sut.crawlAllWorkspaces();

        crawlStartedForOriginWithSeeds("http://host1", List.of("http://host1", "http://host1/path1", "http://host1/path2"));
        verifyNoMoreCrawls();
    }

    private void crawlStartedFor(String origin) {
        crawlStartedForOriginWithSeeds(origin, List.of(origin));
    }


    private void crawlStartedForOriginWithSeeds(String origin, List<String> seeds) {
        Object[] expectedUri = seeds.stream().map(URI::create).toArray();

        verify(crawlFactory).build(eq(create(origin).resolve("/")),
                argThat(argument -> new HamcrestArgumentMatcher<>(containsInAnyOrder(expectedUri)).matches(argument)),
                eq(WorkspaceCrawler.DEFAULT_NUM_CONNECTIONS));

        mockJobs.forEach(job -> verify(job).start());
    }

    private void verifyNoMoreCrawls() {
        verifyNoMoreInteractions(crawlFactory);
        mockJobs.forEach(Mockito::verifyNoMoreInteractions);
    }

    private WorkspaceBuilder givenAWorkspace() {
        return new WorkspaceBuilder();
    }

    private class WorkspaceBuilder {


        private final Workspace curWorkspace;

        private WorkspaceBuilder() {
            curWorkspace = new Workspace();
            curWorkspace.setCrawlerSettings(new CrawlerSettings(DEFAULT_NUM_CONNECTIONS, true));
        }

        public WorkspaceBuilder withWebsiteUrl(String s) {
            curWorkspace.setWebsiteUrl(s);
            return this;
        }

        public void build() {
            allWorkspaces.add(curWorkspace);
        }

    }
}