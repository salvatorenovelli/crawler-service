package com.myseotoolbox.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myseotoolbox.archive.ArchiveServiceClient;
import com.myseotoolbox.crawler.config.AppConfig;
import com.myseotoolbox.crawler.model.PageSnapshot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@RestClientTest
@Import({AppConfig.class, ArchiveServiceClient.class})
public class ArchiveServiceClientTest {

    public static final String TEST_PAGE_URI = "http://host/a";
    public static final String MISSING_PAGE_URI = "http://host/b";

    @Autowired private RestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;


    @Autowired ArchiveServiceClient sut;
    private PageSnapshot expectedSnapshot = aPageSnapshotWithStandardValuesForUri(TEST_PAGE_URI);
    private MockRestServiceServer server;


    @Before
    public void setUp() {
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldContactArchiveService() throws JsonProcessingException {
        String snapshotString = objectMapper.writeValueAsString(expectedSnapshot);
        server.expect(requestTo("/archive-api/page?uri=" + TEST_PAGE_URI)).andRespond(withSuccess(snapshotString, MediaType.APPLICATION_JSON));

        Optional<PageSnapshot> lastPageSnapshot = sut.getLastPageSnapshot(TEST_PAGE_URI);

        server.verify();
        assertEquals(lastPageSnapshot.get(), expectedSnapshot);
    }

    @Test
    public void shouldReturnEmptyInCaseOf404() {
        server.expect(requestTo("/archive-api/page?uri=" + MISSING_PAGE_URI)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<PageSnapshot> lastPageSnapshot = sut.getLastPageSnapshot(MISSING_PAGE_URI);

        server.verify();
        assertFalse(lastPageSnapshot.isPresent());
    }
}