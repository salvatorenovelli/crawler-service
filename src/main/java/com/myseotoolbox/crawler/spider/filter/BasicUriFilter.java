package com.myseotoolbox.crawler.spider.filter;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractHostPort;

@Slf4j
public class BasicUriFilter implements Predicate<URI> {
    private static final Pattern EXCLUDED = Pattern.compile(".*\\.(?:css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz)$");
    private final URI websiteOrigin;

    public BasicUriFilter(URI websiteOrigin) {
        this.websiteOrigin = websiteOrigin;
    }

    @Override
    public boolean test(URI uri) {
        String str = uri.toString().toLowerCase();

        boolean extension = validExtension(str);
        boolean host = validHost(uri);

        if (!(extension && host)) {
            log.debug("Filtering uri. ValidExtension: {} ValidHost: {} URI: {}", extension, host, uri);
        }

        return extension && host;
    }

    private boolean validExtension(String str) {
        return !EXCLUDED.matcher(str).matches();
    }

    private boolean validHost(URI uri) {
        return extractHostPort(uri).equals(extractHostPort(websiteOrigin));
    }


}
