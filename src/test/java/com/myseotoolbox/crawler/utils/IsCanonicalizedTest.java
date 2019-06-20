package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.testutils.PageSnapshotTestBuilder;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class IsCanonicalizedTest {

    @Test
    public void isSelfCanonicalized() {
        PageSnapshot snapshot = PageSnapshotTestBuilder
                .aTestPageSnapshotForUri("http://testhost")
                .withCanonicals("http://testHost/dst")
                .withRedirectChainElements(el(301, "http://testHost/dst"), el(200, "http://testHost/dst"))
                .build();

        assertFalse(IsCanonicalized.isCanonicalizedToDifferentUri(snapshot));
    }

    @Test
    public void isCanonicalizedToDifferentUri() {
        PageSnapshot snapshot = PageSnapshotTestBuilder
                .aTestPageSnapshotForUri("http://testhost")
                .withCanonicals("http://testHost/anotherDst")
                .withRedirectChainElements(el(301, "http://testHost/dst"), el(200, "http://testHost/dst"))
                .build();

        assertTrue(IsCanonicalized.isCanonicalizedToDifferentUri(snapshot));
    }

    @Test
    public void shouldReturnFalseWithNullRedirectChain() {
        PageSnapshot snapshot = PageSnapshotTestBuilder.aTestPageSnapshotForUri("http://testhost")
                .withCanonicals("http://testHost/dst")
                .withNullRedirectChain().build();
        assertFalse(IsCanonicalized.isCanonicalizedToDifferentUri(snapshot));
    }

    @Test
    public void shouldConsiderDestinationUri() {
        PageSnapshot snapshot = PageSnapshotTestBuilder
                .aTestPageSnapshotForUri("http://testhost/sourceUri")
                .withCanonicals("http://testHost/destinationUri")
                .withRedirectChainElements(el(301, "http://testHost/dst"), el(200, "http://testHost/destinationUri"))
                .build();

        assertFalse(IsCanonicalized.isCanonicalizedToDifferentUri(snapshot));
    }

    private RedirectChainElement el(int statusCode, String url) {
        return new RedirectChainElement(null, statusCode, url);
    }
}