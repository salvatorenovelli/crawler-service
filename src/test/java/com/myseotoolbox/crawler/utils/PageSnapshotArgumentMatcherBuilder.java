package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;

import static org.hamcrest.CoreMatchers.any;


public class PageSnapshotArgumentMatcherBuilder {

    private Matcher<String> uri = any(String.class);
    private Matcher<String> title = any(String.class);

    public static PageSnapshotArgumentMatcherBuilder pageSnapshot() {
        return new PageSnapshotArgumentMatcherBuilder();
    }

    public PageSnapshotArgumentMatcherBuilder withUri(String uri) {
        this.uri = Matchers.is(uri);
        return this;
    }

    public PageSnapshotArgumentMatcherBuilder withTitle(String title) {
        this.title = Matchers.is(title);
        return this;
    }

    public PageSnapshot build() {
        return ArgumentMatchers.argThat(new ArgumentMatcher<PageSnapshot>() {

            @Override
            public boolean matches(PageSnapshot compare) {
                return uri.matches(compare.getUri()) &&
                        title.matches(compare.getTitle());
            }

            @Override
            public String toString() {
                return describeExpected();
            }

            private String describeExpected() {
                return
                        "PageSnapshot{" +
                                "uri= " + uri + '\'' +
                                ", title= " + title + '\'' +
                                '}';
            }
        });
    }

}
