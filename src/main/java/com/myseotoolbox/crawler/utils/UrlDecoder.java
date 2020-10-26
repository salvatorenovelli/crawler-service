package com.myseotoolbox.crawler.utils;

import lombok.SneakyThrows;
import org.springframework.web.util.UriUtils;

import java.net.URISyntaxException;

public class UrlDecoder {
    @SneakyThrows
    public static String decode(String str) {
        try {
            return UriUtils.decode(str, "UTF-8");
        } catch (IllegalArgumentException e) {
            throw new URISyntaxException(str, e.toString());
        }
    }
}
