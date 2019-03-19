package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.spider.model.WebsiteCrawlLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WebsiteCrawlLogRepository extends MongoRepository<WebsiteCrawlLog, String> {
    Optional<WebsiteCrawlLog> findTopByOriginOrderByDateDesc(String origin);
}
