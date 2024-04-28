package com.myseotoolbox.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.TestWorkspaceBuilder;
import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static com.myseotoolbox.crawler.spider.CrawlerSettingsBuilder.defaultSettings;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = AdminWorkspaceCrawlStartController.class)
@RunWith(SpringRunner.class)
public class AdminWorkspaceCrawlStartControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CrawlJobFactory factory;
    @MockBean private WorkspaceRepository repository;
    @MockBean private WorkspaceCrawler workspaceCrawler;
    @MockBean private CrawlEventDispatchFactory cedfactory;
    @MockBean private HTTPClient httpClient;
    private List<Workspace> allWorkspaces = new ArrayList<>();

    @MockBean private CrawlJob job;
    @Autowired private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        when(factory.build(any(), any())).thenReturn(job);
    }

    @Test
    public void testCrawlWorkspace() throws Exception {

        throw new UnsupportedOperationException("Not implemented yet!");
//        // Setup mock workspace
//        givenAWorkspace().withSequenceNumber(123).withWebsiteUrl("http://host1").build();
//
//        CrawlWorkspaceRequest request = new CrawlWorkspaceRequest(123, 1);
//
//
//        String content = objectMapper.writeValueAsString(request);
//        mockMvc.perform(post("/crawl-workspace")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(content))
//                .andDo(print())
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Crawling http://example.com with 3 parallel connections. Started on")));
//
//        verify(job).start();
    }

    private TestWorkspaceBuilder givenAWorkspace() {
        return new TestWorkspaceBuilder(allWorkspaces, null);
    }

}

