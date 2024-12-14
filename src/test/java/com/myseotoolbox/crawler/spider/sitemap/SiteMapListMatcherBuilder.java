package com.myseotoolbox.crawler.spider.sitemap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;

public class SiteMapListMatcherBuilder {

    private Integer listSize = null;
    private List<SiteMapMatcherBuilder> expectedSitemaps = new ArrayList<>();


    public static SiteMapListMatcherBuilder isSitemapList() {
        return new SiteMapListMatcherBuilder();
    }

    public SiteMapListMatcherBuilder withSitemapCount(int count) {
        listSize = count;
        return this;
    }

    public Matcher<List<SiteMap>> build() {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(List<SiteMap> sitemaps) {
                return listSize == null || hasSize(listSize).matches(sitemaps)
                        && matchesSitemaps(sitemaps);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(describeExpected());
            }

            private String describeExpected() {
                return "List<SiteMap> of size" + listSize + "\n" +
                        "         With:<" + expectedSitemaps + '>';
            }
        };
    }

    private boolean matchesSitemaps(List<SiteMap> actualSitemaps) {
        return expectedSitemaps.stream().allMatch(expectedSitemap -> {
            SiteMap actualSitemap = findSitemap(expectedSitemap.getSitemapLocation(), actualSitemaps);
            if (actualSitemap == null) return false;
            return expectedSitemap.buildMatcher().matches(actualSitemap);
        });
    }

    private SiteMap findSitemap(URI uri, List<SiteMap> sitemaps) {
        return sitemaps.stream()
                .filter(data -> data.location().equals(uri))
                .findFirst()
                .orElse(null);
    }

    public SiteMapMatcherBuilder havingSitemapFor(URI uri) {
        return SiteMapMatcherBuilder.aSiteMapFor(this, uri);
    }

    void addSitemap(SiteMapMatcherBuilder sitemap) {
        this.expectedSitemaps.add(sitemap);
    }
}