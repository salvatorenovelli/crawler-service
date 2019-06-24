package com.myseotoolbox.crawler.outboundlink;

import com.myseotoolbox.crawler.model.CrawlResult;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class OutboundLinksListener implements Consumer<CrawlResult> {
    private final String crawlId;
    private final OutboundLinkRepository repository;

    public OutboundLinksListener(String crawlId, OutboundLinkRepository repository) {
        this.crawlId = crawlId;
        this.repository = repository;
    }

    @Override
    public void accept(CrawlResult crawlResult) {
        List<Link> links = crawlResult.getPageSnapshot()
                .getLinks()
                .stream()
                .map(url -> new Link(Link.Type.AHREF, url))
                .collect(Collectors.toList());

        repository.save(new OutboundLink(null, crawlId, crawlResult.getUri(), links));
    }
}
