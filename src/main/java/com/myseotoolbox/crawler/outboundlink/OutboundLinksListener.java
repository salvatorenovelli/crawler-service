package com.myseotoolbox.crawler.outboundlink;

import com.myseotoolbox.crawler.model.CrawlResult;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalizedToDifferentUri;


public class OutboundLinksListener implements Consumer<CrawlResult> {
    private final String crawlId;
    private final OutboundLinkRepository repository;

    public OutboundLinksListener(String crawlId, OutboundLinkRepository repository) {
        this.crawlId = crawlId;
        this.repository = repository;
    }

    @Override
    public void accept(CrawlResult crawlResult) {

        if (isCanonicalizedToDifferentUri(crawlResult.getPageSnapshot())) return;

        HashMap<LinkType, List<String>> linkTypeListHashMap = new HashMap<>();
        linkTypeListHashMap.put(LinkType.AHREF, crawlResult.getPageSnapshot().getLinks().stream().distinct().collect(Collectors.toList()));

        repository.save(new OutboundLinks(null, crawlId, crawlResult.getUri(), linkTypeListHashMap));
    }
}
