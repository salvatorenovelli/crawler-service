package com.myseotoolbox.crawler.utils;

public class EnsureRange {
    public static int ensureRange(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
