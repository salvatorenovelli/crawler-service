package com.myseotoolbox.crawler.spider.filter;

import com.myseotoolbox.crawler.spider.UriFilter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.regex.Pattern;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.extractHostPort;

@Slf4j
public class BasicUriFilter implements UriFilter {
    private static final Pattern INVALID_EXTENSIONS = Pattern.compile(".*\\.(?:css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz)$");
    private static final Pattern VALID_SCHEME = Pattern.compile("(?:http|https)$");


    private final URI websiteOrigin;
    private final String originPath;

    public BasicUriFilter(URI websiteOrigin) {
        this.websiteOrigin = websiteOrigin;
        this.originPath = addTrailingSlashIfMissing(websiteOrigin.getPath());
    }

    @Override
    public boolean shouldCrawl(URI sourceUri, URI discoveredLink) {

        boolean valid = validScheme(discoveredLink) && validExtension(discoveredLink) && validHost(discoveredLink) && validPath(sourceUri, discoveredLink);

        if (!valid && log.isDebugEnabled()) {
            log.debug("Blocked: {} URI: {} src: {}",
                    getFilterCause(validScheme(discoveredLink), validExtension(discoveredLink), validHost(discoveredLink), validPath(sourceUri, discoveredLink)),
                    discoveredLink,
                    sourceUri);
        }

        return valid;

    }

    private String getFilterCause(boolean scheme, boolean extension, boolean host, boolean path) {
        return (!scheme ? "SCH " : "") + (!extension ? "EXT " : "") + (!host ? "HOST " : "") + (!path ? "PATH " : "");
    }

    private boolean validPath(URI sourceUri, URI discoveredLink) {
        return isChildOfOrigin(sourceUri) || isChildOfOrigin(discoveredLink);
    }

    private boolean validExtension(URI str) {
        return !INVALID_EXTENSIONS.matcher(str.toString().toLowerCase()).matches();
    }

    private boolean validHost(URI uri) {
        return extractHostPort(uri).equals(extractHostPort(websiteOrigin));
    }

    private boolean isChildOfOrigin(URI base) {
        return addTrailingSlashIfMissing(base.getPath()).startsWith(originPath);
    }

    private String addTrailingSlashIfMissing(String path) {
        return path + (path.endsWith("/") ? "" : "/");
    }

    private boolean validScheme(URI discoveredLink) {
        String scheme = discoveredLink.getScheme();
        return scheme != null && VALID_SCHEME.matcher(scheme).matches();
    }
}
