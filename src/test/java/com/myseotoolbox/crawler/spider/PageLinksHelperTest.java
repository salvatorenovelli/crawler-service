package com.myseotoolbox.crawler.spider;

import com.myseotoolbox.crawler.pagelinks.PageLink;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PageLinksHelperTest {

    @Test
    public void shouldRemoveNofollowLinks() {
        PageLink valid = new PageLink("/dst0", Collections.emptyMap());
        PageLink invalid = new PageLink("/dst1", Collections.singletonMap("rel", "nofollow"));


        List<URI> filtered = PageLinksHelper.filterValidPageLinks(Arrays.asList(invalid, valid));

        assertThat(filtered, hasSize(1));
        assertThat(filtered.get(0).toString(), is("/dst0"));
    }

    @Test
    public void shouldNotRemoveOtherRel() {
        List<URI> filtered = PageLinksHelper.filterValidPageLinks(
                Collections.singletonList(new PageLink("/dst0", Collections.singletonMap("rel", "next")))
        );

        assertThat(filtered, hasSize(1));
        assertThat(filtered.get(0).toString(), is("/dst0"));
    }
}