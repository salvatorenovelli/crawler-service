package com.myseotoolbox.crawler.pagelinks;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboundLinkRepository extends MongoRepository<OutboundLinks, String> {}
