package com.myseotoolbox.crawler.outboundlink;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OutboundLinkRepository extends MongoRepository<OutboundLink, String> {
    Optional<OutboundLink> findByCrawlIdAndUrl(String crawlId, String url);
}
