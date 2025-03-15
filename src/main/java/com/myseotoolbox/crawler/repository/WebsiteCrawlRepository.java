package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.websitecrawl.WebsiteCrawl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WebsiteCrawlRepository extends MongoRepository<WebsiteCrawl, String>, CustomWebsiteCrawlRepository {
}

interface CustomWebsiteCrawlRepository {
    Optional<WebsiteCrawl> findLatestByWorkspace(int workspaceNumber);
}

@RequiredArgsConstructor
class CustomWebsiteCrawlRepositoryImpl implements CustomWebsiteCrawlRepository {

    private final MongoOperations mongoOperations;

    @Override
    public Optional<WebsiteCrawl> findLatestByWorkspace(int workspaceNumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("trigger.targetWorkspaces").is(workspaceNumber));
        query.with(Sort.by(Sort.Direction.DESC, "startedAt"));
        query.limit(1);
        return Optional.ofNullable(mongoOperations.findOne(query, WebsiteCrawl.class));
    }
}
