package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.MonitoredUri;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface MonitoredUriRepository extends MongoRepository<MonitoredUri, String> {


    List<MonitoredUri> findByUri(@Param("uri") String uri);

    List<MonitoredUri> findAllByWorkspaceNumber(int workspaceNumber);

    long countByWorkspaceNumber(int workspaceNumber);

}





