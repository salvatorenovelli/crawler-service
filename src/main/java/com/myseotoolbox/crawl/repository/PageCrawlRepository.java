package com.myseotoolbox.crawl.repository;


import com.myseotoolbox.crawl.model.PageCrawl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface PageCrawlRepository extends MongoRepository<PageCrawl, String> {
    Optional<PageCrawl> findTopByUriOrderByCreateDateDesc(@Param("uri") String uri);
}