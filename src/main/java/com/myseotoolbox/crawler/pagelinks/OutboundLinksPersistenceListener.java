package com.myseotoolbox.crawler.pagelinks;

import com.myseotoolbox.crawler.model.CrawlResult;
import com.myseotoolbox.crawler.model.RedirectChainElement;
import com.myseotoolbox.crawler.spider.PageLinksHelper;
import com.myseotoolbox.crawler.spider.event.PageCrawledEvent;
import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import com.myseotoolbox.crawlercommons.UriCreator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.spider.filter.WebsiteOriginUtils.isHostMatching;
import static com.myseotoolbox.crawler.utils.GetDestinationUri.getDestinationUriString;

@Component
public class OutboundLinksPersistenceListener {
    private final OutboundLinkRepository repository;

    public OutboundLinksPersistenceListener(OutboundLinkRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void onPageCrawled(PageCrawledEvent pageCrawledEvent) {
        CrawlResult crawlResult = pageCrawledEvent.getCrawlResult();

        if (isValid(crawlResult)) {
            processCrawlResult(pageCrawledEvent.getWebsiteCrawl(), crawlResult);
        }
    }

    private boolean isValid(CrawlResult crawlResult) {
        List<RedirectChainElement> redirectChainElements = crawlResult.getPageSnapshot().getRedirectChainElements();
        return redirectChainElements != null && !redirectChainElements.isEmpty();
    }

    private void processCrawlResult(WebsiteCrawl websiteCrawl, CrawlResult crawlResult) {
        HashMap<LinkType, List<String>> linkTypeListHashMap = new HashMap<>();
        String destUri = getDestinationUriString(crawlResult.getPageSnapshot());

        linkTypeListHashMap.put(LinkType.AHREF, getLinks(crawlResult, URI.create(websiteCrawl.getOrigin()), destUri));
        linkTypeListHashMap.put(LinkType.CANONICAL, getCanonicals(crawlResult, URI.create(websiteCrawl.getOrigin()), destUri));

        repository.save(new OutboundLinks(null, websiteCrawl.getId(), crawlResult.getUri(), LocalDateTime.now(), URI.create(crawlResult.getUri()).getHost(), linkTypeListHashMap));
    }

    private List<String> getCanonicals(CrawlResult crawlResult, URI origin, String destUri) {
        List<String> canonicals = crawlResult.getPageSnapshot().getCanonicals();
        if (canonicals == null || canonicals.isEmpty()) return Collections.emptyList();
        return normalize(origin, destUri, canonicals);
    }

    private List<String> getLinks(CrawlResult crawlResult, URI origin, String destUri) {
        List<PageLink> links = crawlResult.getPageSnapshot().getLinks();
        if (links == null || links.isEmpty()) return Collections.emptyList();
        List<PageLink> followableLinks = PageLinksHelper.filterFollowablePageLinks(links);
        return normalize(origin, destUri, PageLinkMapper.toLinkUrls(followableLinks));
    }

    private List<String> normalize(URI origin, String destUri, List<String> links) {
        return links.stream()
                .map(PageLinksHelper::toValidUri)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(link -> relativize(origin, destUri, link))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private String relativize(URI origin, String pageUrl, URI linkUri) {

        try {

            URI pageUri = UriCreator.create(pageUrl);

            if (isRelativeUrl(linkUri) || hostMatches(origin, linkUri)) {
                URI resolved = pageUri.resolve(linkUri);
                if (hostMatches(origin, pageUri)) {
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

    private boolean hostMatches(URI origin, URI linkUri) {
        return schemeMatching(origin, linkUri) && isHostMatching(origin, linkUri);
    }

    private boolean schemeMatching(URI origin, URI linkUri) {
        return Objects.equals(origin.getScheme(), linkUri.getScheme());
    }

}
