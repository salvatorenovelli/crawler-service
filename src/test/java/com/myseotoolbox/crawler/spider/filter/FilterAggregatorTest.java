package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilterAggregatorTest {

    private static final URI BASE = URI.create("http://host1");
    @Mock private UriFilter predicate1;
    @Mock private UriFilter predicate2;
    @Mock private UriFilter predicateFalse;

    @Before
    public void setUp() throws Exception {
        when(predicate1.shouldCrawl(any(), any())).thenReturn(true);
        when(predicate2.shouldCrawl(any(), any())).thenReturn(true);
        when(predicateFalse.shouldCrawl(any(), any())).thenReturn(false);
    }

    @Test
    public void shouldCheckAllPredicates() {
        FilterAggregator sut = new FilterAggregator((s, d) -> true, (s, d) -> false);
        assertFalse(sut.shouldCrawl(BASE, URI.create("http://host1")));
    }

    @Test
    public void shouldCheckAllPredicatesTrue() {
        FilterAggregator sut = new FilterAggregator((s, d) -> true, (s, d) -> true);
        assertTrue(sut.shouldCrawl(BASE, URI.create("http://host1")));
    }

    @Test
    public void shouldCheckAllPredicatesBothFalse() {
        FilterAggregator sut = new FilterAggregator((s, d) -> false, (s, d) -> false);
        assertFalse(sut.shouldCrawl(BASE, URI.create("http://host1")));
    }

    @Test
    public void shouldPassTheParameter() {
        FilterAggregator sut = new FilterAggregator(predicate1, predicate2);

        URI uri = URI.create("http://host1");
        sut.shouldCrawl(BASE, uri);

        verify(predicate1).shouldCrawl(BASE, uri);
        verify(predicate2).shouldCrawl(BASE, uri);
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void shouldSkipSecondIfFirstFail() {
        FilterAggregator sut = new FilterAggregator(predicate1, predicateFalse, predicate2);

        URI uri = URI.create("http://host1");
        sut.shouldCrawl(BASE, uri);

        verify(predicate1).shouldCrawl(BASE, uri);
        verify(predicateFalse).shouldCrawl(BASE, uri);
        verifyNoMoreInteractions(predicate1, predicateFalse, predicate2);
    }


}