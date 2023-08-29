package com.myseotoolbox.crawler.utils;


import javax.annotation.Nullable;

public class EnsureRange {

    /**
     * Ensures that the provided input is confined within the given minimum and maximum bounds.
     * <p>
     * This method accepts an input integer and evaluates it against the provided minimum and maximum bounds.
     * If the input value is less than the minimum or greater than the maximum, the method adjusts it to
     * fit within the specified range.
     * </p>
     * <ul>
     *     <li>If the input is null, it defaults to 0.</li>
     *     <li>If the minimum is not provided (i.e., null), the input is ensured to be no less than {@link Integer#MIN_VALUE}.</li>
     *     <li>If the maximum is not provided (i.e., null), the input is ensured to be no more than {@link Integer#MAX_VALUE}.</li>
     * </ul>
     *
     * @param input The integer value to be checked and potentially adjusted.
     * @param min   The minimum allowed value. If null, the lower bound is set to {@link Integer#MIN_VALUE}.
     * @param max   The maximum allowed value. If null, the upper bound is set to {@link Integer#MAX_VALUE}.
     * @return The adjusted input value ensuring it lies within the provided or default bounds.
     */
    public static int ensureRange(@Nullable Integer input, @Nullable Integer min, @Nullable Integer max) {
        return Math.max(nullSafeMin(min), Math.min(nullSafeZero(input), nullSafeMax(max)));
    }

    public static long ensureRange(@Nullable Long input, @Nullable Long min, @Nullable Long max) {
        return Math.max(nullSafeMin(min), Math.min(nullSafeZero(input), nullSafeMax(max)));
    }


    private static int nullSafeZero(Integer val) {
        return val != null ? val : 0;
    }

    private static long nullSafeZero(Long val) {
        return val != null ? val : 0L;
    }


    private static int nullSafeMin(Integer val) {
        return val != null ? val : Integer.MIN_VALUE;
    }

    private static long nullSafeMin(Long val) {
        return val != null ? val : Long.MIN_VALUE;
    }


    private static int nullSafeMax(Integer val) {
        return val != null ? val : Integer.MAX_VALUE;
    }

    private static long nullSafeMax(Long val) {
        return val != null ? val : Long.MAX_VALUE;
    }
}
