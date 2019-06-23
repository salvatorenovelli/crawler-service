package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChain;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.spider.model.SnapshotTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aPageSnapshotWithStandardValuesForUri;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CrawlerQueueTest {

    private static final UriFilter NO_URI_FILTER = (s, d) -> true;
    private static final int MAX_CRAWLS = 100;
    private static final String LOCATION_WITH_UNICODE_CHARACTERS = "/família";
    private static final String QUEUE_NAME = "name";


    private CrawlerQueue sut;
    @Mock private CrawlersPool pool;
    @Mock private Consumer<CrawlResult> mockListener;

    @Before
    public void setUp() {
        doAnswer(invocation -> {
            SnapshotTask task = invocation.getArgument(0);
            task.getTaskRequester().accept(CrawlResult.forSnapshot(aPageSnapshotWithStandardValuesForUri(task.getUri().toString())));
            return null;
        }).when(pool).accept(any());
    }


    @Test
    public void shouldRequireProcessingOfAllSeeds() {
        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();
        verify(pool).accept(taskForUri("http://host1"));
    }


    @Test
    public void shouldNotVisitTheSameUrlTwice() {

        whenCrawling("http://host1").discover("http://host1");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
    }

    @Test
    public void shouldNotifyListenersWithSnapshotResult() {
        whenCrawling("http://host1").discover("http://host1/dst");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.subscribeToPageCrawled(mockListener);
        sut.start();

        verify(mockListener).accept(argThat(argument -> argument.getUri().equals("http://host1")));
    }

    @Test
    public void shouldNotVisitTwiceIfRelativeWasVisitedAndAbsoluteIsDiscovered() {

        whenCrawling("http://host1").discover("/dst");
        whenCrawling("http://host1/dst").discover("http://host1/dst");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst"));

    }

    @Test
    public void shouldVisitTwiceIfDifferentVersionOfRootIsDiscovered() {

        whenCrawling("http://host1").discover("/");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/"));
    }


    @Test
    public void takeAlwaysReturnAbsoluteUri() {
        whenCrawling("http://host1").discover("/dst");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst"));
    }


    @Test
    public void onSnapshotCompleteShouldNeverBeFedRelativeUrisAsTakeNeverReturnsIt() {

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1/dst"), pool, NO_URI_FILTER, MAX_CRAWLS);

        try {
            sut.accept(CrawlResult.forSnapshot(aPageSnapshotWithStandardValuesForUri("/dst")));
        } catch (IllegalStateException e) {
            //success!!
            assertThat(e.getMessage(), containsString("URI should be absolute"));
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void completingASnapshotNeverSubmittedShouldThrowException() {

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1/dst"), pool, NO_URI_FILTER, MAX_CRAWLS);

        try {
            sut.accept(CrawlResult.forSnapshot(aPageSnapshotWithStandardValuesForUri(("http://host1/dst"))));
        } catch (IllegalStateException e) {
            //success!!
            assertThat(e.getMessage(), containsString("never submitted"));
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void shouldAlertIfCompleteTheSameSnapshotTwice() {

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        try {
            sut.accept(CrawlResult.forSnapshot(aPageSnapshotWithStandardValuesForUri(("http://host1"))));
        } catch (IllegalStateException e) {
            //success!!
            assertThat(e.getMessage(), containsString("already completed"));
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void shouldProcessAllSeedsWhenMultipleSeedsAreFed() {

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1", "http://host2"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host2"));

    }


    @Test
    public void canResolveLinksWithUnicodeChars() {

        whenCrawling("http://host1").discover(LOCATION_WITH_UNICODE_CHARACTERS);


        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);

        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1" + "/fam%C3%ADlia"));


    }

    @Test
    public void discoveringDuplicateLinksInPageDoesNotEnqueueItMultipleTimes() {
        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2", "http://host1/dst1");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
        verify(pool).accept(taskForUri("http://host1/dst2"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);

    }

    @Test
    public void shouldNotAcceptFilteredUris() {
        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2", "http://host1/shouldReject");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, (s, d) -> !d.toString().equals("http://host1/shouldReject"), MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
        verify(pool).accept(taskForUri("http://host1/dst2"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);

    }

    @Test
    public void submittingDuplicatedUrlWhileItIsInProgressShouldNotQueueItTwice() {

        whenCrawling("http://host1").discover("http://host1/dst1", "http://host1/dst2");

        //dst1 is never submitted to simulate that is taking longer than dst2 crawl
        doAnswer(invocation -> null).when(pool).accept(taskForUri("http://host1/dst1"));


        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();


        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
        verify(pool).accept(taskForUri("http://host1/dst2"));
    }

    @Test
    public void canHandleInvalidEmptyJavascriptLinks() {
        whenCrawling("http://host1").discover("javascript:{}", "/dst1");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/dst1"));
    }

    @Test
    public void canHandleInvalidLink() {
        whenCrawling("http://host1").discover("not +[a valid link", "/dst1");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
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
            task.getTaskRequester().accept(CrawlResult.forSnapshot(t));
            return null;
        }).when(pool).accept(taskForUri("http://host1"));

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));

    }

    @Test
    public void shouldIgnoreFragment() {
        whenCrawling("http://host1").discover("http://host1#someFragment");
        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);

    }


    @Test
    public void canManageLinkWithFragmentOnly() {
        whenCrawling("http://host1").discover("#");
        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void canManageEmptyLinks() {
        whenCrawling("http://host1").discover("%20#");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldBeAbleToHandleURIWithEncodedSpaces() {
        whenCrawling("http://host1").discover("/this%20destination%20contains%20spaces");
        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/this%20destination%20contains%20spaces"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldHandleUriWithLeadingSpaces() {
        whenCrawling("http://host1").discover("/leadingspaces    ");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/leadingspaces"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldBeTolerantWithLinksContainingSpacesInTheMiddle() {
        whenCrawling("http://host1").discover("/link with spaces");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();


        System.out.println(mockingDetails(pool).printInvocations());

        verify(pool).accept(taskForUri("http://host1"));
        verify(pool).accept(taskForUri("http://host1/link%20with%20spaces"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldEnqueueCanonicalLinkIfDifferentFromUri() {

        String baseUri = "http://host1/base?t=12345";
        String canonicalPath = "http://host1/base";

        initMocksToReturnCanonical(baseUri, canonicalPath);


        sut = new CrawlerQueue(QUEUE_NAME, uris(baseUri), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();


        verify(pool).accept(taskForUri(baseUri));
        verify(pool).accept(taskForUri(canonicalPath));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);

    }

    @Test
    public void shouldNotCrawlTwiceWhenCanonicalIsSameAsUri() {
        String baseUri = "http://host1/base";
        String canonicalPath = "http://host1/base";

        initMocksToReturnCanonical(baseUri, canonicalPath);

        sut = new CrawlerQueue(QUEUE_NAME, uris(baseUri), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verify(pool).accept(taskForUri(baseUri));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }


    @Test
    public void shouldNotNotifyListenersForBlockedRedirectChains() {
        whenCrawling("http://host1").redirectToBlockedUrl("http://host1/blockedByRobots");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        verifyNoMoreInteractions(mockListener);
    }

    @Test
    public void shouldShutdownPoolWhenFinished() {

        whenCrawling("http://host1/").discover("http://host1/path0");
        whenCrawling("http://host1/path0").discover("http://host1/path1");
        whenCrawling("http://host1/path1").discover("http://host1/path2");
        whenCrawling("http://host1/path2").discover("http://host1/path3");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1/"), pool, NO_URI_FILTER, MAX_CRAWLS);
        sut.start();

        Mockito.inOrder(pool);
        verify(pool).accept(taskForUri("http://host1/"));
        verify(pool).accept(taskForUri("http://host1/path0"));
        verify(pool).accept(taskForUri("http://host1/path1"));
        verify(pool).accept(taskForUri("http://host1/path2"));
        verify(pool).accept(taskForUri("http://host1/path3"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldNotCrawlMoreThanSpecified_discoverOneByOne() {
        whenCrawling("http://host1/").discover("http://host1/1");
        whenCrawling("http://host1/1").discover("http://host1/2");
        whenCrawling("http://host1/2").discover("http://host1/3");
        whenCrawling("http://host1/3").discover("http://host1/should-not-get-here");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1/"), pool, NO_URI_FILTER, 4);
        sut.start();

        verify(pool).accept(taskForUri("http://host1/"));
        verify(pool).accept(taskForUri("http://host1/1"));
        verify(pool).accept(taskForUri("http://host1/2"));
        verify(pool).accept(taskForUri("http://host1/3"));
        verify(pool).shutDown();

        System.out.println(mockingDetails(pool).printInvocations());

        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldNotCrawlMoreThanSpecified_discoverAllAtOnce() {
        whenCrawling("http://host1/").discover("http://host1/1", "http://host1/2", "http://host1/3", "http://host1/should-not-get-here");

        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1/"), pool, NO_URI_FILTER, 4);
        sut.start();

        verify(pool).accept(taskForUri("http://host1/"));
        verify(pool).accept(taskForUri("http://host1/1"));
        verify(pool).accept(taskForUri("http://host1/2"));
        verify(pool).accept(taskForUri("http://host1/3"));
        verify(pool).shutDown();

        System.out.println(mockingDetails(pool).printInvocations());

        verifyNoMoreInteractions(pool);
    }

    @Test
    public void shouldNotCrawlDuplicateSeeds() {
        sut = new CrawlerQueue(QUEUE_NAME, uris("http://host1/", "http://host1/"), pool, NO_URI_FILTER, 4);
        sut.start();
        verify(pool).accept(taskForUri("http://host1/"));
        verify(pool).shutDown();
        verifyNoMoreInteractions(pool);
    }

    private List<URI> uris(String... s) {
        return Stream.of(s).map(URI::create).collect(Collectors.toList());
    }

    private CrawlerQueueTestMockBuilder whenCrawling(String baseUri) {
        return new CrawlerQueueTestMockBuilder(baseUri);
    }


    private URI uri(String uri) {
        return URI.create(uri);
    }

    private void initMocksToReturnCanonical(String baseUri, String canonicalPath) {
        doAnswer(invocation -> {
            SnapshotTask task = invocation.getArgument(0);
            PageSnapshot t = aPageSnapshotWithStandardValuesForUri(task.getUri().toString());
            t.setCanonicals(Collections.singletonList(canonicalPath));

            CrawlResult result = CrawlResult.forSnapshot(t);
            task.getTaskRequester().accept(result);
            return null;
        }).when(pool).accept(taskForUri(baseUri));
    }

    private class CrawlerQueueTestMockBuilder {

        private final String baseUri;

        CrawlerQueueTestMockBuilder(String baseUri) {
            this.baseUri = baseUri;
        }

        void discover(String... discoveredUris) {
            doAnswer(invocation -> {
                SnapshotTask task = invocation.getArgument(0);
                PageSnapshot t = aPageSnapshotWithStandardValuesForUri(task.getUri().toString());
                t.setLinks(Arrays.asList(discoveredUris));
                task.getTaskRequester().accept(CrawlResult.forSnapshot(t));
                return null;
            }).when(pool).accept(taskForUri(baseUri));
        }

        public void redirectToBlockedUrl(String redirectDestination) {
            doAnswer(invocation -> {
                SnapshotTask task = invocation.getArgument(0);
                RedirectChain chain = new RedirectChain();
                chain.addElement(new RedirectChainElement(baseUri, 301, redirectDestination));
                task.getTaskRequester().accept(CrawlResult.forBlockedChain(chain));
                return null;
            }).when(pool).accept(taskForUri(baseUri));
        }
    }

    private SnapshotTask taskForUri(String uri) {
        return argThat(arg -> arg.getUri().equals(uri(uri)));
    }
}