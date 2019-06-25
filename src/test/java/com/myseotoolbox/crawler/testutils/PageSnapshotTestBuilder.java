package com.myseotoolbox.crawler.testutils;


import com.myseotoolbox.crawler.model.PageSnapshot;
import com.myseotoolbox.crawler.model.RedirectChainElement;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.StandardMetaTagValues.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PageSnapshotTestBuilder {
    private PageSnapshot cur = new PageSnapshot();

    public PageSnapshotTestBuilder(String uri) {
        cur.setUri(uri);
        cur.setRedirectChainElements(buildRedirectChainElementsFor(uri,200));
        cur.setCreateDate(new Date());
    }

    public static PageSnapshot aPageSnapshotWith404ValuesForUri(String uri) {
        PageSnapshot pageSnapshot = new PageSnapshot(uri, "", emptyList(), emptyList(), emptyList(), emptyList());
        pageSnapshot.setRedirectChainElements(buildRedirectChainElementsFor(uri, 404));
        return pageSnapshot;
    }

    public static PageSnapshot aPageSnapshotWithStandardValuesForUri(String uri) {
        PageSnapshot pageSnapshot = new PageSnapshot(uri, STANDARD_TITLE, STANDARD_H1, STANDARD_H2, STANDARD_META_DESCR, singletonList(uri));
        pageSnapshot.setRedirectChainElements(STANDARD_REDIRECT_CHAIN_ELEMENTS);
        return pageSnapshot;
    }

    public static PageSnapshotTestBuilder aTestPageSnapshotForUri(String s) {
        return new PageSnapshotTestBuilder(s);
    }

    public PageSnapshotTestBuilder withTitle(String title) {
        cur.setTitle(title);
        return this;
    }

    public PageSnapshotTestBuilder withH1s(String... h1s) {
        cur.setH1s(Arrays.asList(h1s));
        return this;
    }

    public PageSnapshotTestBuilder withH2s(String... h2) {
        cur.setH2s(Arrays.asList(h2));
        return this;
    }

    public PageSnapshotTestBuilder withCanonicals(String... canonicals) {
        cur.setCanonicals(Arrays.asList(canonicals));
        return this;
    }

    public PageSnapshotTestBuilder withMetas(String... metas) {
        cur.setMetaDescriptions(Arrays.asList(metas));
        return this;
    }


    public static List<RedirectChainElement> buildRedirectChainElementsFor(String uri, int... statuses) {
        AtomicInteger dstId = new AtomicInteger(0);


        return Arrays.stream(statuses)
                .mapToObj(status ->
                        new RedirectChainElement(uri + "" + dstId.getAndIncrement(), status, uri + "" + dstId.getAndIncrement())
                )
                .collect(Collectors.toList());
    }

    public PageSnapshot build() {
        return cur;
    }

    public PageSnapshotTestBuilder withNullRedirectChain() {
        cur.setRedirectChainElements(null);
        return this;
    }

    public PageSnapshotTestBuilder withRedirectChainElements(int... statusCodes) {
        cur.setRedirectChainElements(buildRedirectChainElementsFor(cur.getUri(), statusCodes));
        return this;
    }

    public PageSnapshotTestBuilder withRedirectChainElements(RedirectChainElement... elements) {
        cur.setRedirectChainElements(Arrays.asList(elements));
        return this;
    }

    public PageSnapshotTestBuilder withLinks(String ...links) {
        cur.setLinks(Arrays.asList(links));
        return this;
    }

    public PageSnapshotTestBuilder withNullLinks() {
        cur.setLinks(null);
        return this;
    }
}
