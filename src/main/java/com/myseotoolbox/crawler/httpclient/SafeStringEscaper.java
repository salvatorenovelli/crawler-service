package com.myseotoolbox.crawler.httpclient;

public class SafeStringEscaper {


    private final static char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };


    public static boolean containsUnicodeCharacters(String locationHeaderField) {
        for (int i = 0; i < locationHeaderField.length(); i++) {
            if (locationHeaderField.charAt(i) >= 128) return true;
        }
        return false;
    }

    /**
     * Escape non-ascii characters prefixing % without decoding them.
     */
    public static String escapeString(String s) {

        int n = s.length();
        if (n == 0)
            return s;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; ) {
            char c = s.charAt(i);
            if (c >= '\u0080') {
                appendEscape(sb, (byte) (c & 0xFF));
            } else {
                sb.append(c);
            }
            if (++i >= n)
                break;
        }

        return sb.toString();
    }

    private static void appendEscape(StringBuilder sb, byte b) {
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b >> 0) & 0x0f]);
    }
}
