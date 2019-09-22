package com.myseotoolbox.crawler.utils;

import java.net.URI;

public class LinkResolver {


    public static URI resolve(URI sourceUri, String path) {
        return addTrailingSlashIfRoot(sourceUri).resolve(path);
    }

    private static URI addTrailingSlashIfRoot(URI sourceUri) {
        return sourceUri.getPath().isEmpty() ? sourceUri.resolve("/") : sourceUri;
    }
}
