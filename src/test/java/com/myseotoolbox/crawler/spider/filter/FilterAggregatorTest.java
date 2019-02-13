package com.myseotoolbox.crawler.spider.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FilterAggregatorTest {

    @Mock private Predicate<URI> predicate1;
    @Mock private Predicate<URI> predicate2;
    @Mock private Predicate<URI> predicateFalse;

    @Before
    public void setUp() throws Exception {
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicateFalse.test(any())).thenReturn(false);
    }

    @Test
    public void shouldCheckAllPredicates() {
        FilterAggregator sut = new FilterAggregator(uri -> true, uri -> false);
        assertFalse(sut.test(URI.create("http://host1")));
    }

    @Test
    public void shouldCheckAllPredicatesTrue() {
        FilterAggregator sut = new FilterAggregator(uri -> true, uri -> true);
        assertTrue(sut.test(URI.create("http://host1")));
    }

    @Test
    public void shouldCheckAllPredicatesBothFalse() {
        FilterAggregator sut = new FilterAggregator(uri -> false, uri -> false);
        assertFalse(sut.test(URI.create("http://host1")));
    }

    @Test
    public void shouldPassTheParameter() {
        FilterAggregator sut = new FilterAggregator(predicate1, predicate2);

        URI uri = URI.create("http://host1");
        sut.test(uri);

        verify(predicate1).test(uri);
        verify(predicate2).test(uri);
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void shouldSkipSecondIfFirstFail() {
        FilterAggregator sut = new FilterAggregator(predicate1, predicateFalse, predicate2);

        URI uri = URI.create("http://host1");
        sut.test(uri);

        verify(predicate1).test(uri);
        verify(predicateFalse).test(uri);
        verifyNoMoreInteractions(predicate1, predicateFalse, predicate2);
    }


}