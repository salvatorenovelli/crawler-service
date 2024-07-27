package com.myseotoolbox.crawler.utils;


import static jakarta.servlet.http.HttpServletResponse.*;

public class IsRedirect {
    public static boolean isRedirect(int statusCode) {
        switch (statusCode) {
            case SC_MOVED_PERMANENTLY: // 301
            case SC_MOVED_TEMPORARILY: // 302
            case SC_SEE_OTHER: // 303
            case SC_TEMPORARY_REDIRECT: // 307
            case 308:
                return true;
            default:
                return false;
        }
    }
}
