package com.myseotoolbox.crawler;

import com.myseotoolbox.crawler.httpclient.SnapshotException;
import com.myseotoolbox.crawler.httpclient.WebPageReader;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.testutils.TestWebsite;
import com.myseotoolbox.crawler.testutils.testwebsite.ReceivedRequest;
import com.myseotoolbox.crawler.testutils.testwebsite.TestWebsiteBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


@SuppressWarnings("unchecked")
public class WebPageReaderTest {

    public static final String TEST_TITLE = "Test withTitle";
    public static final String TEST_ROOT_PAGE_PATH = "/";
    public static final String TEST_REDIRECT_URL = "/another_url";


    private WebPageReader sut;
    private TestWebsiteBuilder testWebsiteBuilder = TestWebsiteBuilder.build();

    @Before
    public void setUp() {
        sut = new WebPageReader();
    }

    @After
    public void tearDown() throws Exception {
        testWebsiteBuilder.tearDown();
    }

    @Test
    public void shouldRetrieveTitle() throws Exception {
        givenAWebsite().havingRootPage()
                .withTitle(TEST_TITLE)
                .run();
        PageSnapshot snapshot = sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));
        assertThat(snapshot.getTitle(), is(TEST_TITLE));
    }

    @Test
    public void shouldReadTags() throws Exception {
        givenAWebsite().havingRootPage()
                .withTag("H1", "Test H1")
                .run();
        PageSnapshot snapshot = sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));

        List<String> h1s = snapshot.getH1s();

        assertThat(h1s, hasSize(1));
        assertThat(h1s.get(0), is("Test H1"));
    }

    @Test
    public void shouldReadMultipleTags() throws Exception {
        givenAWebsite().havingRootPage()
                .withTag("H1", "Test H1")
                .withTag("H1", "Test second H1")
                .run();
        PageSnapshot snapshot = sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));

        List<String> h1s = snapshot.getH1s();

        assertThat(h1s, hasSize(2));
        assertThat(h1s.get(0), is("Test H1"));
        assertThat(h1s.get(1), is("Test second H1"));
    }

    @Test
    public void shouldReadMultipleDiverseTags() throws Exception {
        givenAWebsite().havingRootPage()
                .withTag("H1", "Test H1")
                .withTag("H1", "Test second H1")
                .withTag("H2", "Test H2")
                .run();

        PageSnapshot snapshot = sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));

        List<String> h1s = snapshot.getH1s();
        List<String> h2s = snapshot.getH2s();

        assertThat(h1s, hasSize(2));
        assertThat(h2s, hasSize(1));

        assertThat(h2s.get(0), is("Test H2"));

    }

    @Test
    public void destinationUrlShouldBeSavedAsAbsolute() throws Exception {

        givenAWebsite()
                .havingPage(TEST_ROOT_PAGE_PATH).redirectingTo(301, "/dst").and()
                .havingPage("/dst").withTitle("You've reached the right place!")
                .run();

        PageSnapshot pageSnapshot = sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));

        assertThat(getDestinationUri(pageSnapshot), is(testUri("/dst").toString()));
        assertThat(pageSnapshot.getTitle(), is("You've reached the right place!"));
    }


    @Test
    public void redirectChainIsSaved() throws Exception {

        givenAWebsite()
                .havingPage("/").redirectingTo(301, "/dst1").and()
                .havingPage("/dst1").redirectingTo(302, "/dst2").and()
                .havingPage("/dst2").redirectingTo(301, "/dst3").and()
                .havingPage("/dst3").withTitle("You've reached the right place!")
                .run();

        PageSnapshot pageSnapshot = sut.snapshotPage(testUri("/"));

        assertThat(pageSnapshot.getRedirectChainElements(), contains(
                el(301, "/dst1"),
                el(302, "/dst2"),
                el(301, "/dst3"),
                el(200, "/dst3")));

    }


    @Test
    public void pageNotFound404ShouldBeHandled() throws Exception {

        givenAWebsite()
                .havingPage("/page").redirectingTo(301, "/dst1")
                .run();

        PageSnapshot pageSnapshot = sut.snapshotPage(testUri("/page"));

        assertThat(pageSnapshot.getRedirectChainElements(), contains(
                el(301, "/dst1"),
                el(404, "/dst1")));


    }

    @Test
    public void redirectLoopShouldBeHandledGracefully() throws Exception {
        givenAWebsite()
                .havingPage("/start").redirectingTo(301, "/dst1").and()
                .havingPage("/dst1").redirectingTo(301, "/dst2").and()
                .havingPage("/dst2").redirectingTo(301, "/start")
                .run();


        try {
            sut.snapshotPage(testUri("/start"));
        } catch (SnapshotException e) {

            assertThat(e.getPartialSnapshot().getRedirectChainElements(), contains(
                    el(301, "/dst1"),
                    el(301, "/dst2"),
                    el(301, "/start")));

            assertThat(e.getPartialSnapshot().getCrawlStatus(), containsString("ERROR: Redirect Loop"));
            return;
        }

        fail("Expected exception");


    }

    @Test
    public void shouldHaveAPoliteUserAgent() throws Exception {

        TestWebsite website = givenAWebsite().havingRootPage()
                .withTitle(TEST_TITLE)
                .run();

        sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));

        List<ReceivedRequest> requests = website.getRequestsReceived();

        assertThat(requests.get(0).getUserAgent(), is("Mozilla/5.0 (compatible; SeoBot/1.0)"));

    }


    @Test
    public void shouldPerformOneRequestWhenNoRedirectIsRequired() throws Exception {
        TestWebsite website = givenAWebsite().havingRootPage()
                .withTitle(TEST_TITLE)
                .run();

        sut.snapshotPage(testUri(TEST_ROOT_PAGE_PATH));

        assertThat(website.getRequestsReceived().size(), is(1));

    }


    @Test
    public void shouldPerformOneRequestPerRedirect() throws Exception {
        TestWebsite website = givenAWebsite()
                .havingPage("/start").redirectingTo(301, "/dst1").and()
                .havingPage("/dst1").redirectingTo(301, "/dst2").and()
                .havingPage("/dst2").redirectingTo(301, "/start")
                .run();

        try {
            sut.snapshotPage(testUri("/start"));
            fail("Expected Exception");
        } catch (SnapshotException e) {
            //We expect the exception
        }

        assertThat(website.getRequestsReceived().size(), is(3));
    }

    @Test
    public void itShouldOnlyConsumeSupportedMimeType() throws Exception {
        //content-type should be text/*

        givenAWebsite()
                .havingRootPage().withMimeType("application/pdf")
                .run();


        try {
            sut.snapshotPage(testUri("/"));
        } catch (SnapshotException e) {
            assertThat(e.getPartialSnapshot().getCrawlStatus(), containsString("Unhandled content type"));
            return;
        }

        fail("Expected exception");

    }

    @Test
    public void inCaseOfExceptionShouldReturnAppropriateDefaults() {
        try {
            sut.snapshotPage(URI.create("/"));
            fail("Expected exception");
        } catch (SnapshotException e) {
            PageSnapshot defaultVal = e.getPartialSnapshot();
            assertThat(defaultVal.getUri(), is("/"));
            assertNotNull(defaultVal.getCreateDate());
            assertNotNull(defaultVal.getRedirectChainElements());
            assertThat(defaultVal.getCrawlStatus(), containsString("Unable to crawl:"));
        }
    }

    private String getDestinationUri(PageSnapshot pageSnapshot) {
        List<RedirectChainElement> redirectChainElements = pageSnapshot.getRedirectChainElements();
        return redirectChainElements.get(redirectChainElements.size() - 1).getDestinationURI();
    }

    private URI testUri(String s) throws URISyntaxException {
        return testWebsiteBuilder.buildTestUri(s);
    }

    private TestWebsiteBuilder givenAWebsite() {
        return testWebsiteBuilder;
    }

    private Matcher<RedirectChainElement> el(int status, String dstUri) {

        String uri = testWebsiteBuilder.buildTestUri(dstUri).toString();

        return new BaseMatcher<RedirectChainElement>() {
            @Override
            public boolean matches(Object item) {
                RedirectChainElement element = (RedirectChainElement) item;
                return element.getHttpStatus() == status && element.getDestinationURI().equals(uri);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("{ " + status + ", '" + uri + "' }");
            }
        };
    }
}