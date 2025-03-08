package com.myseotoolbox.crawler.spider.sitemap;

import com.myseotoolbox.crawler.httpclient.HttpRequestFactory;
import com.myseotoolbox.crawler.httpclient.HttpResponse;
import com.myseotoolbox.crawler.spider.UriFilter;
import com.myseotoolbox.crawler.utils.UriUtils;
import com.myseotoolbox.crawlercommons.UriCreator;
import crawlercommons.sitemaps.*;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.myseotoolbox.crawler.utils.WebsiteOriginUtils.isHostMatching;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class SiteMapReader {

    private final URI origin;
    private final UriFilter uriFilter;
    private final int crawledPageLimit;
    private final List<URI> siteMaps;
    private final HttpRequestFactory requestFactory;

    private SiteMapParser siteMapParser = new SiteMapParser(false);

    private int currentUriCount = 0;

    /**
     * How SiteMapReader is instantiated:
     * <ol>
     *   <li>Create a robots.txt file based on the ROOT of the workspace (or workspaces).</li>
     *   <li>Set the origin by resolving "/" on the aggregated websiteUrl.</li>
     *   <li>Retrieve the sitemap URL from origin/robots.txt. (passed here as `sitemaps`)</li>
     *   <li>Create a uriFilter based on the most permissive sub-path using
     *       {@link com.myseotoolbox.crawler.spider.configuration.AllowedPathFromSeeds#extractAllowedPathFromSeeds(java.util.Collection)}.</li>
     * </ol>
     */
    public SiteMapReader(URI origin, List<String> sitemaps, UriFilter uriFilter, int crawledPageLimit, HttpRequestFactory requestFactory) {
        this.origin = origin;
        this.siteMaps = sitemaps.stream().map(this::mapToUrIOrLogWarning).filter(Objects::nonNull).collect(toList());
        this.uriFilter = uriFilter;
        this.crawledPageLimit = crawledPageLimit;
        this.requestFactory = requestFactory;
    }

    public List<SiteMap> fetchSitemaps() {
        return this.siteMaps
                .stream()
                .flatMap(this::fetch)
                .toList();
    }

    /**
     * Recursively scan Sitemap Index (the type of sitemap that has links to other sitemaps) and collect URLs
     */
    private Stream<SiteMap> fetch(URI location) {

        if (!shouldFetch(location)) {
            return Stream.empty();
        }

        if (crawledPageLimitExceeded()) {
            log.warn("Fetching stopped for sitemap {} due to exceeding limit of {}. Current: {}", location, crawledPageLimit, currentUriCount);
            return Stream.empty();
        }

        try {
            log.debug("Fetching sitemap on {}", location);
            AbstractSiteMap asm = fetchSitemap(location);
            if (asm.isIndex()) {
                return traverseSiteMapIndex(asm);
            } else {
                return getSiteMapData(location, asm);
            }
        } catch (URISyntaxException | UnknownFormatException | IOException e) {
            log.warn("Error while fetching sitemap for {}. Error: {}", location, e.toString());
            return Stream.empty();
        }
    }

    private AbstractSiteMap fetchSitemap(URI location) throws IOException, URISyntaxException, UnknownFormatException {
        HttpResponse response = requestFactory.buildGetFor(location).execute();
        if (response.getHttpStatus() == HttpStatus.OK.value()) {
            return siteMapParser.parseSiteMap(response.getContentType(), IOUtils.toByteArray(response.getInputStream()), location.toURL());
        }
        throw new IOException(response + " - " + location.toString());
    }

    private Stream<SiteMap> traverseSiteMapIndex(AbstractSiteMap asm) {
        return ((SiteMapIndex) asm).getSitemaps().stream()
                .map(AbstractSiteMap::getUrl)
                .map(this::mapToUrIOrLogWarning)
                .filter(Objects::nonNull)
                .flatMap(this::fetch);

    }

    private Stream<SiteMap> getSiteMapData(URI location, AbstractSiteMap asm) {
        crawlercommons.sitemaps.SiteMap siteMap = (crawlercommons.sitemaps.SiteMap) asm;
        Set<URI> uriList = siteMap.getSiteMapUrls().stream()
                .map(this::mapToUrIOrLogWarning)
                .filter(Objects::nonNull)
                .filter(uri -> uriFilter.shouldCrawl(uri, uri))
                .filter(this::isSameDomain)
                .collect(toSet());

        if (currentUriCount + uriList.size() > crawledPageLimit) {
            log.warn("Sitemap {}->{} contains more urls than the allowed limit {}/{}", origin, location, currentUriCount + uriList.size(), crawledPageLimit);
            uriList = uriList.stream().limit(crawledPageLimit - currentUriCount).collect(toSet());
        }

        currentUriCount += uriList.size();
        return Stream.of(new SiteMap(location, uriList));
    }

    private boolean crawledPageLimitExceeded() {
        return currentUriCount >= crawledPageLimit;
    }

    private boolean shouldFetch(URI uri) {
        try {

            if (uri == null) {
                return false;
            }

            if (!isSameDomain(uri)) {
                return false;
            }

            return isInRoot(uri) || this.siteMaps.contains(uri) || uriFilter.shouldCrawl(uri, uri);
        } catch (IllegalArgumentException e) {
            log.warn("Unable to fetch sitemap on {}. {}", uri, e.toString());
            return false;
        }
    }

    private boolean isInRoot(URI url) {
        try {
            return UriUtils.getFolder(url.toString()).equals("/");
        } catch (MalformedURLException e) {
            log.warn("Malformed URL " + url, e);
            return false;
        }
    }

    private boolean isSameDomain(String url) {
        return isHostMatching(UriCreator.create(url), origin, false);
    }

    private boolean isSameDomain(URI url) {
        return isSameDomain(url.toString());
    }

    private URI mapToUrIOrLogWarning(SiteMapURL siteMapURL) {
        return mapToUrIOrLogWarning(siteMapURL.getUrl());
    }


    private URI mapToUrIOrLogWarning(URL s) {
        return mapToUrIOrLogWarning(s.toString());
    }

    private URI mapToUrIOrLogWarning(String s) {
        return Try.of(() -> URI.create(s)).onFailure(throwable -> log.warn("Unable to crawl sitemap on {}. Error: {}", s, throwable.toString())).getOrElse((URI) null);
    }
}
