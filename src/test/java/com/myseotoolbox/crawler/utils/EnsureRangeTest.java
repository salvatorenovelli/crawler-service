package com.myseotoolbox.crawler.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnsureRangeTest {
    @Test
    public void testEnsureRange() {
        assertEquals(5, EnsureRange.ensureRange(5, 0, 10));
        assertEquals(0, EnsureRange.ensureRange(-5, 0, 10));
        assertEquals(10, EnsureRange.ensureRange(15, 0, 10));
        assertEquals(0, EnsureRange.ensureRange(null, 0, 10));
        assertEquals(5, EnsureRange.ensureRange(5, null, 10));
        assertEquals(-100, EnsureRange.ensureRange(-100, null, 10));
        assertEquals(5, EnsureRange.ensureRange(5, 0, null));
        assertEquals(5, EnsureRange.ensureRange(5, null, null));
        Integer nothing = null;
        assertEquals(0, EnsureRange.ensureRange(nothing, nothing, nothing));
    }
}