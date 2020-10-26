package com.myseotoolbox.crawler.utils;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UrlDecoderTest {

    @Test
    public void basicDecoding() {
        assertThat(UrlDecoder.decode("http://host/link%20with%20spaces"),is("http://host/link with spaces"));
    }

    @Test
    public void ShouldLeavePlusSignAlone() {
        assertThat(UrlDecoder.decode("http://host/link+link"),is("http://host/link+link"));
    }

    @Test(expected = URISyntaxException.class)
    public void shouldThrowUriSyntaxExceptionInCaseOfWrongEncoding() {
        UrlDecoder.decode("http://host?%%20");
    }
}