package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CrawlManagerTest {

    public static final String LOCATION_WITH_UNICODE_CHARACTERS = "/família";


    CrawlManager sut;
    @Mock private CrawlersPool pool;

    @Before
    public void setUp() throws Exception {
        doAnswer(invocation -> {
            SnapshotTask task = invocation.getArgument(0);
            task.getTaskRequester().accept(aPageSnapshotWithStandardValuesForUri(task.getUri().toString()));
            return null;
        }).when(pool).accept(any());
    }


    @Test
    public void shouldRequireProcessingOfAllSeeds() {
        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();
        verify(pool).accept(taskForUri("http://host1"));
    }


    @Test
    public void shouldNotVisitTheSameUrlTwice() {

        whenCrawling("http://host1").discover("http://host1");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
    }

    @Test
    public void shouldNotVisitTwiceIfRelativeWasVisitedAndAbsoluteIsDiscovered() {

        whenCrawling("http://host1").discover("/dst");
        whenCrawling("http://host1/dst").discover("http://host1/dst");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst"));

    }

    @Test
    public void shouldVisitTwiceIfDifferentVersionOfRootIsDiscovered() {

        whenCrawling("http://host1").discover("/");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/"));
    }


    @Test
    public void takeAlwaysReturnAbsoluteUri() {
        whenCrawling("http://host1").discover("/dst");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst"));
    }


    @Test
    public void onSnapshotCompleteShouldNeverBeFedRelativeUrisAsTakeNeverReturnsIt() {

        sut = new CrawlManager(uris("http://host1/dst"), pool, uri -> true);

        try {
            sut.accept(aPageSnapshotWithStandardValuesForUri("/dst"));
        } catch (IllegalStateException e) {
            //success!!
            assertThat(e.getMessage(), containsString("URI should be absolute"));
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void completingASnapshotNeverSubmittedShouldThrowException() {

        sut = new CrawlManager(uris("http://host1/dst"), pool, uri -> true);

        try {
            sut.accept(aPageSnapshotWithStandardValuesForUri(("http://host1/dst")));
        } catch (IllegalStateException e) {
            //success!!
            assertThat(e.getMessage(), containsString("never submitted"));
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void shouldAlertIfCompleteTheSameSnapshotTwice() {

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        try {
            sut.accept(aPageSnapshotWithStandardValuesForUri(("http://host1")));
        } catch (IllegalStateException e) {
            //success!!
            assertThat(e.getMessage(), containsString("already completed"));
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void shouldProcessAllSeedsWhenMultipleSeedsAreFed() throws InterruptedException {

        sut = new CrawlManager(uris("http://host1", "http://host2"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host2"));

    }


    @Test
    public void canResolveLinksWithUnicodeChars() {

        whenCrawling("http://host1").discover(LOCATION_WITH_UNICODE_CHARACTERS);


        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1" + "/fam%EDlia"));


    }

    @Test
    public void discoveringDuplicateLinksInPageDoesNotEnqueueItMultipleTimes() {
        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2", "http://host1/dst1");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
        verify(pool).accept(taskForUri("http://host1/dst2"));
        verifyNoMoreInteractions(pool);

    }

    @Test
    public void shouldNotAcceptFilteredUris() {
        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2", "http://host1/shouldReject");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> {
            return !uri.toString().equals("http://host1/shouldReject");
        });
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
        verify(pool).accept(taskForUri("http://host1/dst2"));
        verifyNoMoreInteractions(pool);

    }

    @Test
    public void submittingDuplicatedUrlWhileItIsInProgressShouldNotQueueItTwice() throws InterruptedException {

        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2");

        //dst1 is never submitted to simulate that is taking longer than dst2 crawl
        doAnswer(invocation -> null).when(pool).accept(taskForUri("http://host1/dst1"));


        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();


        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
        verify(pool).accept(taskForUri("http://host1/dst2"));
    }

    @Test
    public void canHandleInvalidEmptyJavascriptLinks() {
        whenCrawling("http://host1").discover("javascript:{}", "/dst1");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
    }

    @Test
    public void canHandleInvalidLink() {
        whenCrawling("http://host1").discover("not a valid link", "/dst1");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
    }

    @Test
    public void canHandleNoLinksAtAll() {
        doAnswer(invocation -> {
            SnapshotTask task = invocation.getArgument(0);
            PageSnapshot t = aPageSnapshotWithStandardValuesForUri(task.getUri().toString());
            t.setLinks(null);
            task.getTaskRequester().accept(t);
            return null;
        }).when(pool).accept(taskForUri("http://host1"));

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));

    }

    @Test
    public void shouldIgnoreFragment() {
        whenCrawling("http://host1").discover("http://host1#someFragment");
        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verifyNoMoreInteractions(pool);

    }


    @Test
    public void canManageLinkWithFragmentOnly() {
        whenCrawling("http://host1").discover("#");
        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void canManageEmptyLinks() {
        whenCrawling("http://host1").discover("%20#");

        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldBeAbleToHandleURIWithSpaces() {
        whenCrawling("http://host1").discover("/this destination contains spaces");
        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldBeAbleToHandleURIWithEncodedSpaces() {
        whenCrawling("http://host1").discover("/this%20destination%20contains%20spaces");
        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/this%20destination%20contains%20spaces"));
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldHandleUriWithLeadingSpaces() {
        whenCrawling("http://host1").discover("/leadingspaces    ");
        sut = new CrawlManager(uris("http://host1"), pool, uri -> true);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/leadingspaces"));
        verifyNoMoreInteractions(pool);
    }

    private List<URI> uris(String... s) {
        return Stream.of(s).map(URI::create).collect(Collectors.toList());
    }

    private CrawlManagerTestMockBuilder whenCrawling(String baseUri) {
        return new CrawlManagerTestMockBuilder(baseUri);
    }


    private URI uri(String uri) {
        return URI.create(uri);
    }

    private class CrawlManagerTestMockBuilder {

        private final String baseUri;

        public CrawlManagerTestMockBuilder(String baseUri) {
            this.baseUri = baseUri;
        }

        public void discover(String... discoveredUris) {
            doAnswer(invocation -> {
                SnapshotTask task = invocation.getArgument(0);
                PageSnapshot t = aPageSnapshotWithStandardValuesForUri(task.getUri().toString());
                t.setLinks(Arrays.asList(discoveredUris));
                task.getTaskRequester().accept(t);
                return null;
            }).when(pool).accept(taskForUri(baseUri));
        }

    }

    private SnapshotTask taskForUri(String uri) {
        return argThat(arg -> arg.getUri().equals(uri(uri)));
    }
}