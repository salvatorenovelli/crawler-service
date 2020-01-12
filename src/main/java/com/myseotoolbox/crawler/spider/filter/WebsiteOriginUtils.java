package com.myseotoolbox.crawler.spider.filter;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import static com.myseotoolbox.crawler.spider.PathMatcher.isSubPath;

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
        return concatHostPort(possibleSubdomain).endsWith("." + concatHostPort(origin));
    }


    public static boolean isHostMatching(URI a, URI b) {
        return isHostMatching(a, b, true);
    }

    /**
     * Non strict will match www.host to host
     */
    public static boolean isHostMatching(URI a, URI b, boolean strictWww) {
        String hostA = concatHostPort(a);
        String hostB = concatHostPort(b);

        if (!strictWww) {
            hostA = hostA.replaceAll("^www\\.", "");
            hostB = hostB.replaceAll("^www\\.", "");
        }


        return hostA.equals(hostB);
    }

    public static boolean isSameOrigin(URI base, URI compare, boolean strictWww) {
        return isSchemeMatching(base, compare) && isHostMatching(base, compare, strictWww);
    }

    public static boolean isChildOf(URI origin, URI possibleChild) {
        return isChildOf(origin, possibleChild, true);
    }

    private static boolean isChildOf(URI origin, URI possibleChild, boolean matchSchema) {
        if (matchSchema && !isSchemeMatching(origin, possibleChild)) return false;
        if (!isHostMatching(origin, possibleChild)) return false;

        return isSubPath(origin.getPath(), possibleChild.getPath());
    }

    private static boolean isSchemeMatching(URI origin, URI possibleChild) {
        return Objects.equals(origin.getScheme(), possibleChild.getScheme());
    }

    public static String extractHostAndPort(URI uri) {
        String portStr = "";
        if (uri.getPort() != -1 && uri.getPort() != 80) {
            portStr = ":" + uri.getPort();
        }
        String host = extractHost(uri);
        return host.toLowerCase() + portStr;
    }

    private static String concatHostPort(URI uri) {
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

    public static URI extractOrigin(URI source) {
        return source.resolve("/");
    }
}
