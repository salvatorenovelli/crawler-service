package com.myseotoolbox.crawler.spider.filter;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class WebsiteOriginUtils {

    public static boolean isValidOrigin(String origin) {

        try {
            URL url = new URI(origin).toURL();
            if (!Arrays.asList("http", "https").contains(url.getProtocol())) return false;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isSubdomain(URI origin, URI possibleSubdomain) {
        return extractHostPort(possibleSubdomain).endsWith("." + extractHostPort(origin));
    }

    public static boolean isHostMatching(URI a, URI b) {
        return extractHostPort(a).equals(extractHostPort(b));
    }

    public static boolean isChildOf(URI origin, URI possibleChild) {
        return isChildOf(origin, possibleChild, true);
    }

    public static boolean isChildOf(URI origin, URI possibleChild, boolean matchSchema) {
        if (matchSchema && !isSchemeMatching(origin, possibleChild)) return false;
        if (!isHostMatching(origin, possibleChild)) return false;

        return isSubPath(origin.getPath(), possibleChild.getPath());
    }

    public static boolean isSubPath(String basePath, String possibleChild) {
        String originPath = addTrailingSlashIfMissing(basePath);
        String possibleChildPath = addTrailingSlashIfMissing(possibleChild);
        return possibleChildPath.startsWith(originPath);
    }


    public static URI extractRoot(URI source) {
        return source.resolve("/");
    }

    private static boolean isSchemeMatching(URI origin, URI possibleChild) {
        return Objects.equals(origin.getScheme(), possibleChild.getScheme());
    }

    public static String addTrailingSlashIfMissing(String path) {
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
