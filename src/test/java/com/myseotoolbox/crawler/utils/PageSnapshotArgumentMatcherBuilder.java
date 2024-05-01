package com.myseotoolbox.crawler.utils;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.mockito.ArgumentMatcher;

public class PageSnapshotArgumentMatcherBuilder {

    private Matcher<String> uri = dummy();
    private Matcher<String> title = dummy();

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

    public ArgumentMatcher<PageSnapshot> build() {
        return new ArgumentMatcher<PageSnapshot>() {

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

            ;

        };
    }

    /**
     * Don't use any() instead of this. Mockito will count the number of matchers and complain you've used the wrong combination
     */
    private <T> Matcher<T> dummy() {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is <any value>");
            }
        };
    }
}
