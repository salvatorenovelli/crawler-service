package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.MonitoredUri;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface MonitoredUriRepository extends MongoRepository<MonitoredUri, String> {


    Optional<MonitoredUri> findByUri(@Param("uri") String uri);

    Page<MonitoredUri> findAllByWorkspaceNumber(int workspaceNumber, Pageable pageable);

    long countByWorkspaceNumber(int workspaceNumber);

}





