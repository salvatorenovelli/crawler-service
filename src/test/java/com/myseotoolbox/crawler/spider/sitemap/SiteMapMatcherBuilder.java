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

    public Matcher<List<SiteMap>> build() {
        and();
        return parent.build();
    }

    Matcher<SiteMap> buildMatcher() {
        return new TypeSafeMatcher<SiteMap>() {
            @Override
            protected boolean matchesSafely(SiteMap siteMap) {
                return linksMatcher.matches(siteMap.links())
                        && sitemapLocation.equals(siteMap.location())
                        && (linkCount == null || hasSize(linkCount).matches(siteMap.links()));
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