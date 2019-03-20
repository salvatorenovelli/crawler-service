package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface WorkspaceRepository extends MongoRepository<Workspace, String> {

}
