package com.myseotoolbox.crawler.websitecrawl;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebsiteCrawlRepository extends MongoRepository<WebsiteCrawl, String> {
}
