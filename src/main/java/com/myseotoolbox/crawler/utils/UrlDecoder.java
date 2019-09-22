package com.myseotoolbox.crawler.utils;

import lombok.SneakyThrows;
import org.springframework.web.util.UriUtils;

public class UrlDecoder {
    @SneakyThrows
    public static String decode(String str) {
        return UriUtils.decode(str, "UTF-8");
    }
}
