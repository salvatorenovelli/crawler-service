package com.myseotoolbox.crawler.spider.filter;

import java.net.URI;
import java.util.Objects;

public class WebsiteOriginUtils {

    public static boolean isSubdomain(URI origin, URI possibleSubdomain) {
        return extractHostPort(possibleSubdomain).endsWith("." + extractHostPort(origin));
    }

    public static boolean isHostMatching(URI a, URI b) {
        return extractHostPort(a).equals(extractHostPort(b));
    }

    public static boolean isChildOf(URI origin, URI possibleChild) {

        if (!isSchemeMatching(origin, possibleChild)) return false;
        if (!isHostMatching(origin, possibleChild)) return false;

        String originPath = addTrailingSlashIfMissing(origin.getPath());
        String possibleChildPath = addTrailingSlashIfMissing(possibleChild.getPath());
        return possibleChildPath.startsWith(originPath);
    }


    public static URI extractOrigin(URI source){
        return source.resolve("/");
    }

    private static boolean isSchemeMatching(URI origin, URI possibleChild) {
        return Objects.equals(origin.getScheme(), possibleChild.getScheme());
    }

    private static String addTrailingSlashIfMissing(String path) {
        return path + (path.endsWith("/") ? "" : "/");
    }

    private static String extractHostPort(URI uri) {
        String portStr = "";
        if (uri.getPort() != -1 && uri.getPort() != 80) {
            portStr = "" + uri.getPort();
        }
        String host = extractHost(uri);
        return host.toLowerCase() + portStr;
    }

    private static String extractHost(URI uri) {
        //can be null in case of mailto: (for some reason I don't understand)
        return uri.getHost() != null ? uri.getHost() : "";
    }
}
