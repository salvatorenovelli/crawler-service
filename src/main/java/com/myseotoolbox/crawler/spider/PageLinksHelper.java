package com.myseotoolbox.crawler.spider;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.myseotoolbox.crawler.utils.RemoveUrlFragment;
import com.myseotoolbox.crawler.utils.UrlDecoder;
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
    private static final Escaper ESCAPER = UrlEscapers.urlFragmentEscaper();

    public List<URI> filterValidLinks(List<String> links) {
        List<URI> filtered = new ArrayList<>();

        if (links != null) {
            filtered = links
                    .stream()
                    .map(PageLinksHelper::toValidUri)
                    .flatMap(this::stream)
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    private <T> Stream<T> stream(Optional<T> opt) {
        return opt.map(Stream::of).orElseGet(Stream::empty);
    }

    public static Optional<URI> toValidUri(String str) {

        if (!isValidUri(str) || str.length() > MAX_URL_LEN) {
            return Optional.empty();
        }

        str = RemoveUrlFragment.removeFragment(str);
        str = str.trim();

        try {
            String decoded = UrlDecoder.decode(str);
            String transCoded = ESCAPER.escape(decoded);

            URI uri = new URI(transCoded);
            if (isEmptyLink(uri)) return Optional.empty();

            return Optional.ofNullable(uri);
        } catch (URISyntaxException e) {
            log.debug("Invalid link: '{}'. {}", str, e.getMessage());
            return Optional.empty();
        }


    }


    private static boolean isEmptyLink(@Nullable URI uri) {
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
