package com.myseotoolbox.crawler.httpclient;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class SafeStringEscaper {

    private static final Escaper escaper = UrlEscapers.urlFragmentEscaper();

    public static boolean containsUnicodeCharacters(String locationHeaderField) {
        for (int i = 0; i < locationHeaderField.length(); i++) {
            if (locationHeaderField.charAt(i) >= 128) return true;
        }
        return false;
    }

    public static String escapeString(String s) {
        return escaper.escape(s);
    }

}
