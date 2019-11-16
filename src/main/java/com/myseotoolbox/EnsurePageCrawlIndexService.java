package com.myseotoolbox;

import com.myseotoolbox.crawler.model.PageCrawl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class EnsurePageCrawlIndexService {

    private final MongoOperations mongoOperations;

    public EnsurePageCrawlIndexService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @PostConstruct
    public void ensureIndex() {
        mongoOperations.indexOps(PageCrawl.class).ensureIndex(new Index().on("uri", Sort.Direction.ASC));
    }
}
