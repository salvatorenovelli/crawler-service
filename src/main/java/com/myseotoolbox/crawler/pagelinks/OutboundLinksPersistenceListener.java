package com.myseotoolbox.crawler.pagelinks;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.spider.PageLinksHelper;
import com.myseotoolbox.crawlercommons.UriCreator;
import org.bson.types.ObjectId;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.isHostMatching;
import static com.myseotoolbox.crawler.utils.GetDestinationUri.getDestinationUriString;


public class OutboundLinksPersistenceListener implements Consumer<CrawlResult> {
    private final ObjectId crawlId;
    private final URI origin;
    private final OutboundLinkRepository repository;

    public OutboundLinksPersistenceListener(ObjectId crawlId, String origin, OutboundLinkRepository repository) {
        this.crawlId = crawlId;
        this.origin = URI.create(origin);
        this.repository = repository;
    }

    @Override
    public void accept(CrawlResult crawlResult) {
        if (isValid(crawlResult)) {
            processCrawlResult(crawlResult);
        }
    }

    private boolean isValid(CrawlResult crawlResult) {
        List<RedirectChainElement> redirectChainElements = crawlResult.getPageSnapshot().getRedirectChainElements();
        return redirectChainElements != null && redirectChainElements.size() > 0;
    }

    private void processCrawlResult(CrawlResult crawlResult) {
        HashMap<LinkType, List<String>> linkTypeListHashMap = new HashMap<>();
        String destUri = getDestinationUriString(crawlResult.getPageSnapshot());

        linkTypeListHashMap.put(LinkType.AHREF, getLinks(crawlResult, destUri));
        linkTypeListHashMap.put(LinkType.CANONICAL, getCanonicals(crawlResult, destUri));

        repository.save(new OutboundLinks(null, crawlId, crawlResult.getUri(), LocalDateTime.now(), URI.create(crawlResult.getUri()).getHost(), linkTypeListHashMap));
    }

    private List<String> getCanonicals(CrawlResult crawlResult, String destUri) {
        List<String> canonicals = crawlResult.getPageSnapshot().getCanonicals();
        if (canonicals == null || canonicals.isEmpty()) return Collections.emptyList();
        return normalize(destUri, canonicals);
    }

    private List<String> getLinks(CrawlResult crawlResult, String destUri) {
        List<PageLink> links = crawlResult.getPageSnapshot().getLinks();
        if (links == null || links.isEmpty()) return Collections.emptyList();
        List<PageLink> followableLinks = PageLinksHelper.filterFollowablePageLinks(links);
        return normalize(destUri, PageLinkMapper.toLinkUrls(followableLinks));
    }

    private List<String> normalize(String destUri, List<String> links) {
        return links.stream()
                .map(PageLinksHelper::toValidUri)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(link -> relativize(destUri, link))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private String relativize(String pageUrl, URI linkUri) {

        try {

            URI pageUri = UriCreator.create(pageUrl);

            if (isRelativeUrl(linkUri) || hostMatches(linkUri)) {
                URI resolved = pageUri.resolve(linkUri);
                if (hostMatches(pageUri)) {
                    return resolved.getRawPath() + getRawQuery(resolved) + getRawFragment(resolved);
                } else {
                    return resolved.toString();
                }
            }

        } catch (IllegalArgumentException e) {
            //nothing to do here, just an invalid link, will return original one.
        }

        return linkUri.toString();
    }

    private String getRawQuery(URI resolved) {
        String rawQuery = resolved.getRawQuery();
        if (isInvalid(rawQuery)) return "";
        return "?" + rawQuery;
    }

    private String getRawFragment(URI resolved) {
        String rawFragment = resolved.getRawFragment();
        if (isInvalid(rawFragment)) return "";
        return "#" + rawFragment;
    }

    private boolean isInvalid(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isRelativeUrl(URI linkUri) {
        return !linkUri.isAbsolute();
    }

    private boolean hostMatches(URI linkUri) {
        return schemeMatching(linkUri) && isHostMatching(origin, linkUri);
    }

    private boolean schemeMatching(URI linkUri) {
        return Objects.equals(origin.getScheme(), linkUri.getScheme());
    }

}
