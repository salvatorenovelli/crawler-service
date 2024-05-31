package com.myseotoolbox.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myseotoolbox.crawler.httpclient.HTTPClient;
import com.myseotoolbox.crawler.model.CrawlWorkspaceRequest;
import com.myseotoolbox.crawler.model.Workspace;
import com.myseotoolbox.crawler.repository.WorkspaceRepository;
import com.myseotoolbox.crawler.spider.CrawlJob;
import com.myseotoolbox.crawler.spider.CrawlJobFactory;
import com.myseotoolbox.crawler.spider.TestWorkspaceBuilder;
import com.myseotoolbox.crawler.spider.WorkspaceCrawler;
import org.bson.types.ObjectId;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminWorkspaceCrawlStartController.class)
@RunWith(SpringRunner.class)
public class AdminWorkspaceCrawlStartControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private CrawlJobFactory factory;
    @MockBean private WorkspaceRepository workspaceRepo;
    @MockBean private WorkspaceCrawler workspaceCrawler;
    @MockBean private CrawlEventDispatchFactory cedfactory;
    @MockBean private HTTPClient httpClient;
    private List<Workspace> allWorkspaces = new ArrayList<>();

    @MockBean private CrawlJob job;
    @Autowired private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        when(factory.build(any(), any())).thenReturn(job);
        when(workspaceRepo.findTopBySeqNumber(anyInt())).thenAnswer(invocation -> {
            int seqNumber = invocation.getArgument(0);
            Workspace workspace = allWorkspaces.stream().filter(ws -> ws.getSeqNumber() == seqNumber).findFirst().get();
            return Optional.of(workspace);
        });
        when(httpClient.get(any())).thenReturn("");
    }

    @Test
    public void testCrawlWorkspace() throws Exception {

        givenAWorkspace().withSequenceNumber(123).withWebsiteUrl("http://host1").build();

        CrawlWorkspaceRequest request = new CrawlWorkspaceRequest(123, 3, "testOwner@myseotoolbox");

        String content = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/crawl-workspace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.crawlId", isValidObjectId()));

        verify(job).start();
    }

    private static Matcher<String> isValidObjectId() {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String hexString) {
                new ObjectId(hexString);
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A valid ObjectID");
            }
        };
    }

    private TestWorkspaceBuilder givenAWorkspace() {
        return new TestWorkspaceBuilder(allWorkspaces, null);
    }

}

