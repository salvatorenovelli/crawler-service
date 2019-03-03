package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface WorkspaceRepository extends MongoRepository<Workspace, String> {

}
