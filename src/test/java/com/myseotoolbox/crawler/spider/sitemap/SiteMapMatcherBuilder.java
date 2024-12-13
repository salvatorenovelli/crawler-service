package com.myseotoolbox.crawler.spider.sitemap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

@RequiredArgsConstructor
@Getter
public class SiteMapMatcherBuilder {
    private final SiteMapListMatcherBuilder parent;
    private final URI sitemapLocation;
    private Integer linkCount = null;
    private Matcher<Iterable<? extends URI>> linksMatcher = Matchers.any((Class<Iterable<? extends URI>>) (Class<?>) Iterable.class);

    public static SiteMapMatcherBuilder aSiteMapFor(SiteMapListMatcherBuilder parent, URI sitemapLocation) {
        return new SiteMapMatcherBuilder(parent, sitemapLocation);
    }

    public SiteMapMatcherBuilder withLinks(URI... links) {
        this.linksMatcher = containsInAnyOrder(links);
        return this;
    }

    public SiteMapMatcherBuilder withLinkCount(int i) {
        linkCount = i;
        return this;
    }

    public SiteMapListMatcherBuilder and() {
        parent.addSitemap(this);
        return parent;
    }

    public Matcher<List<SiteMapData>> build() {
        and();
        return parent.build();
    }

    Matcher<SiteMapData> buildMatcher() {
        return new TypeSafeMatcher<SiteMapData>() {
            @Override
            protected boolean matchesSafely(SiteMapData siteMapData) {
                return linksMatcher.matches(siteMapData.links())
                        && sitemapLocation.equals(siteMapData.location())
                        && (linkCount == null || hasSize(linkCount).matches(siteMapData.links()));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(toString());
            }
        };
    }

    @Override
    public String toString() {
        return "SiteMapData{" +
                "location=" + sitemapLocation +
                ", links=" + linksMatcher + (linkCount != null ? " of size:" + linkCount : "") +
                '}';
    }
}