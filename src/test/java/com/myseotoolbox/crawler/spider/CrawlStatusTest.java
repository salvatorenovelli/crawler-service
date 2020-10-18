package com.myseotoolbox.crawler.spider;

import org.junit.Test;

import java.net.URI;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class CrawlStatusTest {

    public static final URI URI = java.net.URI.create("http://host/uri/");
    public static final URI URI_WITH_FRAGMENT = java.net.URI.create("http://host/uri/#fragment");
    CrawlStatus sut = new CrawlStatus();


    @Test
    public void fragmentsShouldNotBeRemoved() {
        sut.addToInProgress(Arrays.asList(URI, URI_WITH_FRAGMENT));
        assertThat(sut.getTotalEnqueued(), is(2));

        sut.markAsCrawled(URI);
        assertFalse(sut.isCrawlCompleted());
    }
}