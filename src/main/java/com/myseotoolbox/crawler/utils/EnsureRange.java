package com.myseotoolbox.crawler.utils;


import javax.annotation.Nullable;

public class EnsureRange {
    public static int ensureRange(@Nullable Integer value, @Nullable Integer min, @Nullable Integer max) {
        return Math.max(nullSafe(min), Math.min(nullSafe(value), nullSafe(max)));
    }

    private static int nullSafe(Integer val) {
        return val != null ? val : 0;
    }
}
