package com.myseotoolbox.crawler.model;

import org.junit.Test;

import static com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder.aTestPageSnapshotForUri;
import static org.junit.Assert.assertFalse;

public class PageSnapshotTest {

    @Test
    public void shouldNotPrintLinksInToString() {
        PageSnapshot build = aTestPageSnapshotForUri("http://testuri").withLinks("link1").build();
        assertFalse(build.toString().contains("link1"));
    }
}