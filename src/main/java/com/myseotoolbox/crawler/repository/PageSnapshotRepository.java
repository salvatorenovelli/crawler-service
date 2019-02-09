package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.PageSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;


public interface PageSnapshotRepository extends MongoRepository<PageSnapshot, String> {
    Collection<PageSnapshot> findByUri(@Param("uri") String uri);

    Collection<PageSnapshot> findByUriOrderByCreateDateDesc(@Param("uri") String uri);

    Long countByUri(@Param("uri") String uri);

    Collection<PageSnapshot> findByCreateDateAfter(@Param("createDate") Date createDate);
}
