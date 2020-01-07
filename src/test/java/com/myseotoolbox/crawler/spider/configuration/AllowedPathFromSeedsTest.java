package com.myseotoolbox.crawler.spider.configuration;

import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class AllowedPathFromSeedsTest {

    @Test
    public void shouldAddSlashIfNoPathIsPresent() {
        List<String> strings = AllowedPathFromSeeds.extractAllowedPathFromSeeds(Collections.singleton(URI.create("https://host")));
        assertThat(strings, containsInAnyOrder("/"));
    }

    @Test
    public void shouldReturnRootIfSlashIsPresent() {
        List<String> strings = AllowedPathFromSeeds.extractAllowedPathFromSeeds(Collections.singleton(URI.create("https://host/")));
        assertThat(strings, containsInAnyOrder("/"));
    }

    @Test
    public void shouldReturnPath() {
        List<String> strings = AllowedPathFromSeeds.extractAllowedPathFromSeeds(Collections.singleton(URI.create("https://host/path/")));
        assertThat(strings, containsInAnyOrder("/path/"));
    }

    @Test
    public void shouldNotConsiderFilenamesAtTheEnd() {
        List<String> strings = AllowedPathFromSeeds.extractAllowedPathFromSeeds(Collections.singleton(URI.create("https://host/path/index.html")));
        assertThat(strings, containsInAnyOrder("/path/"));
    }
}