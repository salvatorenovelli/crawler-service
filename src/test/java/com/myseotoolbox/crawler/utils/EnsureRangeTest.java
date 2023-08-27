package com.myseotoolbox.crawler.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnsureRangeTest {
    @Test
    public void testEnsureRange() {
        // Test with valid range and value inside range
        assertEquals(5, EnsureRange.ensureRange(5, 0, 10));

        // Test with value below range
        assertEquals(0, EnsureRange.ensureRange(-5, 0, 10));

        // Test with value above range
        assertEquals(10, EnsureRange.ensureRange(15, 0, 10));

        // Test with null input
        assertEquals(0, EnsureRange.ensureRange(null, 0, 10));

        // Test with null min
        assertEquals(5, EnsureRange.ensureRange(5, null, 10));

        assertEquals(-100, EnsureRange.ensureRange(-100, null, 10));

        // Test with null max
        assertEquals(5, EnsureRange.ensureRange(5, 0, null));

        // Test with both min and max as null
        assertEquals(5, EnsureRange.ensureRange(5, null, null));

        // Test with all values as null
        assertEquals(0, EnsureRange.ensureRange(null, null, null));
    }

}