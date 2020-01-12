package com.myseotoolbox.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class ItemMatcher {

    public static <T> Matcher<T> getItemMatcher(Predicate<T> test, String expectedString) {
        return new BaseMatcher<T>() {
            @Override
            public void describeTo(Description description) {
                description.appendText(expectedString);
            }

            @Override
            public boolean matches(Object item) {
                return test.test((T) item);
            }
        };
    }
}
