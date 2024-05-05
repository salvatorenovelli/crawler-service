package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.pagelinks.LinkType;
import com.myseotoolbox.crawler.pagelinks.OutboundLinks;
import org.hamcrest.*;

import static org.hamcrest.CoreMatchers.any;


import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutboundLinksMatcherBuilder {

    private Matcher<ObjectId> crawlId = any(ObjectId.class);
    private Matcher<String> url = any(String.class);
    private Matcher<LocalDateTime> crawledAt = any(LocalDateTime.class);
    private Matcher<String> domain = any(String.class);
    private final Map<LinkType, List<String>> linksByType = new HashMap<>();

    public static OutboundLinksMatcherBuilder outboundLinks() {
        return new OutboundLinksMatcherBuilder();
    }


    public OutboundLinksMatcherBuilder withCrawlId(ObjectId crawlId) {
        this.crawlId = Matchers.is(crawlId);
        return this;
    }

    public OutboundLinksMatcherBuilder withUrl(String url) {
        this.url = Matchers.is(url);
        return this;
    }

    public OutboundLinksMatcherBuilder withCrawledAt(LocalDateTime crawledAt) {
        this.crawledAt = Matchers.is(crawledAt);
        return this;
    }

    public OutboundLinksMatcherBuilder withDomain(String domain) {
        this.domain = Matchers.is(domain);
        return this;
    }

    public OutboundLinksMatcherBuilder withLinks(LinkType linkType, String uri) {
        List<String> links = linksByType.computeIfAbsent(linkType, lt -> new ArrayList<>());
        links.add(uri);
        return this;
    }

    public Matcher<OutboundLinks> build() {
        return new TypeSafeMatcher<OutboundLinks>() {
            @Override
            protected boolean matchesSafely(OutboundLinks compare) {
                return crawlId.matches(compare.getCrawlId()) &&
                        url.matches(compare.getUrl()) &&
                        crawledAt.matches(compare.getCrawledAt()) &&
                        domain.matches(compare.getDomain()) &&
                        matchesLinksByType(compare.getLinksByType());
            }

            private boolean matchesLinksByType(Map<LinkType, List<String>> actual) {
                for (Map.Entry<LinkType, List<String>> entry : linksByType.entrySet()) {
                    if (!actual.containsKey(entry.getKey()) ||
                            !Matchers.hasItems(entry.getValue().toArray()).matches(actual.get(entry.getKey()))) {
                        return false;
                    }
                }
                return true;
            }


            @Override
            public void describeTo(Description description) {
                description.appendText(describeExpected());
            }
        };
    }

    private String describeExpected() {
        return "OutboundLinks{" +
                ", crawlId=" + crawlId +
                ", url=" + url +
                ", crawledAt=" + crawledAt +
                ", domain=" + domain +
                ", linksByType=" + linksByType +
                '}';
    }
}
