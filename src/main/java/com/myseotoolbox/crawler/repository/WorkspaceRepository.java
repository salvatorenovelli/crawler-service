package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface WorkspaceRepository extends MongoRepository<Workspace, String> {

    Optional<Workspace> findTopBySeqNumber(int seqNumber);

}
