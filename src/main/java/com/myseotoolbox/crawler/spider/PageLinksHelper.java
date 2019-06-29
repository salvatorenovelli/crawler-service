package com.myseotoolbox.crawler.spider;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.utils.UriUtils.isValidUri;


@Slf4j
public class PageLinksHelper {

    public static final int MAX_URL_LEN = 1000;

    public List<URI> filterValidLinks(List<String> links) {
        List<URI> filtered = new ArrayList<>();

        if (links != null) {
            filtered = links
                    .stream()
                    .map(this::toValidUri)
                    .flatMap(this::stream)
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    private <T> Stream<T> stream(Optional<T> opt) {
        return opt.map(Stream::of).orElseGet(Stream::empty);
    }

    private Optional<URI> toValidUri(String str) {

        if (!isValidUri(str) || str.length() > MAX_URL_LEN) {
            return Optional.empty();
        }

        str = str.trim();

        try {
            String escaped = str.replaceAll("\\s", "%20");
            URI uri = new URI(escaped);
            uri = removeFragment(uri);
            if (isEmptyLink(uri)) return Optional.empty();

            return Optional.ofNullable(uri);
        } catch (URISyntaxException e) {
            log.debug("Invalid link: '{}'. {}", str, e.getMessage());
            return Optional.empty();
        }


    }

    private URI removeFragment(URI uri) {
        if (uri.getFragment() == null) return uri;
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
        } catch (URISyntaxException e) {
            log.warn("Unable to remove fragment for uri:  {}", uri);
            return null;
        }
    }

    private boolean isEmptyLink(@Nullable URI uri) {
        if (uri == null) return false;
        try {
            String s = URLDecoder.decode(uri.toString(), "UTF-8");
            s = s.trim();
            return s.length() == 0;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

}
