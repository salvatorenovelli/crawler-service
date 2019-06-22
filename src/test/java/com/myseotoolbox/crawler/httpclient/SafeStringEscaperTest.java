package com.myseotoolbox.crawler.httpclient;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SafeStringEscaperTest {
    @Test
    public void canPerformEscapeWithoutDecoding() {
        String result1 = SafeStringEscaper.escapeString("/teg/Ñ\u0083Ñ\u0085Ð¾Ð´-Ð·Ð°-Ð¾Ð´ÐµÐ¶Ð´Ð¾Ð¹");
        assertThat(result1, is("/teg/%C3%91%C2%83%C3%91%C2%85%C3%90%C2%BE%C3%90%C2%B4-%C3%90%C2%B7%C3%90%C2%B0-%C3%90%C2%BE%C3%90%C2%B4%C3%90%C2%B5%C3%90%C2%B6%C3%90%C2%B4%C3%90%C2%BE%C3%90%C2%B9"));
    }


    @Test
    public void name() {
        String result1 = SafeStringEscaper.escapeString("http://test-long-unicode\u200B");
        assertThat(result1, is("http://test-long-unicode%E2%80%8B"));
    }
}