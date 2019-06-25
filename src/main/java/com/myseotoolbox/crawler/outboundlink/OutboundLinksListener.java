package com.myseotoolbox.crawler.outboundlink;

import com.myseotoolbox.crawler.model.CrawlResult;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.myseotoolbox.crawler.utils.IsCanonicalized.isCanonicalizedToDifferentUri;


public class OutboundLinksListener implements Consumer<CrawlResult> {
    private final ObjectId crawlId;
    private final OutboundLinkRepository repository;

    public OutboundLinksListener(ObjectId crawlId, OutboundLinkRepository repository) {
        this.crawlId = crawlId;
        this.repository = repository;
    }

    @Override
    public void accept(CrawlResult crawlResult) {

        if (isCanonicalizedToDifferentUri(crawlResult.getPageSnapshot())) return;

        HashMap<LinkType, List<String>> linkTypeListHashMap = new HashMap<>();
        linkTypeListHashMap.put(LinkType.AHREF, getLinks(crawlResult));

        repository.save(new OutboundLinks(null, crawlId, crawlResult.getUri(), linkTypeListHashMap));
    }

    private List<String> getLinks(CrawlResult crawlResult) {
        List<String> links = crawlResult.getPageSnapshot().getLinks();
        if (links == null) return Collections.emptyList();
        return links.stream().distinct().collect(Collectors.toList());
    }
}
