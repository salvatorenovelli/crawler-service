package com.myseotoolbox.crawl.testutils;

import com.myseotoolbox.crawl.model.ResolvableField;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class PageCrawlMatchers {
    public static BaseMatcher<ResolvableField<?>> referenceTo(ResolvableField<?> expected) {
        return new BaseMatcher<ResolvableField<?>>() {
            @Override
            public boolean matches(Object item) {

                ResolvableField<?> actual = (ResolvableField<?>) item;

                return expected.getReference().equals(actual.getReference());
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expected);
            }
        };
    }

    public static <T> BaseMatcher<ResolvableField<T>> valueType(T expected) {
        return new BaseMatcher<ResolvableField<T>>() {
            @Override
            public boolean matches(Object item) {

                ResolvableField<T> actual = (ResolvableField<T>) item;
                return expected.equals(actual.getValue());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expected.toString());
            }
        };
    }
}
