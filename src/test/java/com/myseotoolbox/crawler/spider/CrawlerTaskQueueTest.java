package com.myseotoolbox.crawler.spider;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class CrawlerTaskQueueTest {

    public static final String LOCATION_WITH_UNICODE_CHARACTERS = "/família";


    CrawlerTaskQueue sut;

    @Test
    public void shouldRequireProcessingOfAllSeeds() {
        sut = new CrawlerTaskQueue(uris("http://host1"));
        assertTrue(sut.mayHaveNext());
    }

    @Test
    public void mayHaveNextReturnFalseOnceAllTheTasksAreCompleted() throws InterruptedException {
        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"));

        URI take1 = sut.take();
        URI take2 = sut.take();

        sut.onSnapshotComplete(take1, Collections.emptyList());
        sut.onSnapshotComplete(take2, Collections.emptyList());

        assertFalse(sut.mayHaveNext());
    }

    @Test
    public void shouldEnqueNewUris() {
        sut = new CrawlerTaskQueue(uris("http://host1"));
        URI take1 = sut.take();
        //discover new url on the page
        sut.onSnapshotComplete(take1, uris("http://host1/dst1"));

        assertTrue(sut.mayHaveNext());
        assertThat(sut.take(), url("http://host1/dst1"));
    }

    @Test
    public void shouldNotVisitTheSameUrlTwice() {
        sut = new CrawlerTaskQueue(uris("http://host1"));

        URI take1 = sut.take();

        //discover "self" url on the page
        sut.onSnapshotComplete(take1, uris("http://host1"));

        assertFalse(sut.mayHaveNext());
    }

    @Test
    public void shouldNotVisitTwiceIfRelativeWasVisitedAndAbsoluteIsDiscovered() {
        sut = new CrawlerTaskQueue(uris("http://host1"));

        URI take1 = sut.take();
        sut.onSnapshotComplete(take1, uris("/dst1"));

        URI takeDst1 = sut.take();
        //discover absolute version of self
        sut.onSnapshotComplete(takeDst1, uris("http://host1/dst1"));

        assertFalse(sut.mayHaveNext());
    }

    @Test
    public void shouldVisitTwiceIfDifferentVersionOfRootIsDiscovered() {
        sut = new CrawlerTaskQueue(uris("http://host1"));

        URI take1 = sut.take();
        sut.onSnapshotComplete(take1, uris("/"));

        assertTrue(sut.mayHaveNext());
    }

    @Test
    public void takeAlwaysReturnAbsoluteUri() {
        sut = new CrawlerTaskQueue(uris("http://host1"));

        URI take1 = sut.take();
        sut.onSnapshotComplete(take1, uris("/dst1"));

        assertThat(sut.take(), url("http://host1/dst1"));
    }

    @Test
    public void onSnapshotCompleteShouldNeverBeFedRelativeUrisAsTakeNeverReturnsIt() {
        sut = new CrawlerTaskQueue(uris("http://host1/dst1"));
        URI take1 = sut.take();

        try {
            sut.onSnapshotComplete(URI.create("/dst"), uris());
        } catch (IllegalStateException e) {
            //success!!
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void shouldAlertIfCompleteTheSameSnapshotTwice() {
        sut = new CrawlerTaskQueue(uris("http://host1/dst1"));
        URI take1 = sut.take();
        sut.onSnapshotComplete(take1, uris());

        try {
            sut.onSnapshotComplete(take1, uris());
        } catch (IllegalStateException e) {
            //success!!
            return;
        }

        fail("Expected exception");
    }

    @Test
    public void shouldRequireProcessingOfAllSeedsWithMultipleSeeds() throws InterruptedException {
        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"));

        URI take = sut.take();

        assertTrue(sut.mayHaveNext());
    }


    @Test
    public void shouldReturnAllSeeds() throws InterruptedException {
        sut = new CrawlerTaskQueue(uris("http://host1", "http://host2"));

        URI take1 = sut.take();
        URI take2 = sut.take();

        assertThat(asList(take1, take2), contains(asList(url("http://host1"), url("http://host2"))));
    }

    @Test
    public void shouldConsiderInProgressTaskAsPotentiallyNewUrls() {
        sut = new CrawlerTaskQueue(uris("http://host1"));
        sut.take();
        assertTrue(sut.mayHaveNext());
    }

    @Test
    public void canResolveLinksWithUnicodeChars() {
        sut = new CrawlerTaskQueue(uris("http://host1"));

        URI take1 = sut.take();
        sut.onSnapshotComplete(take1, uris(LOCATION_WITH_UNICODE_CHARACTERS));

        assertThat(sut.take(), is(url("http://host1" + "/fam%EDlia")));
    }

    private BaseMatcher<URI> url(String expected) {
        return new BaseMatcher<URI>() {
            @Override
            public boolean matches(Object item) {
                URI actual = (URI) item;
                return actual.toString().equals(expected);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expected);
            }
        };
    }

    private List<URI> uris(String... s) {
        return Stream.of(s).map(URI::create).collect(Collectors.toList());
    }
}