package com.myseotoolbox.crawler.spider.filter;

import org.junit.Test;

import java.net.URI;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.*;
import static java.net.URI.create;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class WebsiteOriginUtilsTest {


    @Test
    public void verifySubdomain() {
        assertTrue(isSubdomain(create("http://host"), create("http://www.host")));
    }

    @Test
    public void verifyNotSubdomainSimple() {
        assertFalse(isSubdomain(create("http://host"), create("http://something")));
    }

    @Test
    public void verifyNotSubdomainTricky() {
        assertFalse(isSubdomain(create("http://host"), create("http://anotherhost")));
    }


    @Test
    public void subdomainShouldGoBothWays() {
        assertTrue(areSubdomains(create("http://www.host"), create("http://host"), true));
        assertTrue(areSubdomains(create("http://host"), create("http://www.host"), true));
    }

    @Test
    public void isCaseInsensitive() {
        assertTrue(isHostMatching(create("http://host"), create("http://HOST/differentPath")));
    }

    @Test
    public void shouldMatchDifferentPath() {
        assertTrue(isHostMatching(create("http://host"), create("http://host/differentPath")));
    }


    @Test
    public void shouldDistinguishBetweenHostsWithSameStartWith() {
        assertFalse(isHostMatching(create("http://host"), create("http://host2")));
    }

    @Test
    public void isChildOfShouldMatchDifferentPath() {
        assertTrue(isChildOf(create("http://host"), create("http://host/anotherPath")));
    }

    @Test
    public void isChildOfShouldFilterDifferentHosts() {
        assertFalse(isChildOf(create("http://host"), create("http://differentHost/anotherPath")));
    }

    @Test
    public void isChildOfShouldFilterDifferentProtocol() {
        assertFalse(isChildOf(create("https://host"), create("http://host/path")));
    }

    @Test
    public void shouldBeCaseInsensitive() {
        assertTrue(isChildOf(create("http://host"), create("http://HOST/path")));
    }

    @Test
    public void canRecognizeChildEvenIfOriginHasFilename() {
        assertTrue(isChildOf(create("http://host/index.html"), create("http://host/hello")));
        assertTrue(isChildOf(create("http://host/path/index.html"), create("http://host/path/hello")));
        assertFalse(isChildOf(create("http://host/path/index.html"), create("http://host/outside/hello")));
    }

    @Test
    public void extractOriginShouldKeepProtocol() {
        assertThat(extractOrigin(create("http://host/something")), is(URI.create("http://host/")));
    }

    @Test
    public void isValidOriginShouldFilterNull() {
        assertFalse(isValidOrigin(null));
    }

    @Test
    public void isValidOriginShouldFilterInvalid() {
        assertFalse(isValidOrigin("TBD"));
    }

    @Test
    public void isValidOriginShouldFilterEmpty() {
        assertFalse(isValidOrigin(""));
    }

    @Test
    public void isValidOriginShouldFilterWrongProto() {
        assertFalse(isValidOrigin("ftp://salve"));
    }

    @Test
    public void isValidOriginShouldAllowValid() {
        assertTrue(isValidOrigin("http://salve"));
    }

    @Test
    public void shouldBeStrictOnWwwByDefault() {
        assertFalse(isHostMatching(uri("http://host"), uri("http://www.host")));
    }

    @Test
    public void nonStrictShouldMatchWwwWithNonWww() {
        assertTrue(isHostMatching(URI.create("http://www.host"), URI.create("http://host"), false));
        assertTrue(isHostMatching(URI.create("http://host"), URI.create("http://www.host"), false));
    }

    @Test
    public void hostMatchingShouldNotCareAboutScheme() {
        assertTrue(isHostMatching(URI.create("http://host"), URI.create("https://host"), false));
    }

    private URI uri(String s) {
        return URI.create(s);
    }
}