package com.myseotoolbox.crawler.repository;

import com.myseotoolbox.crawler.model.Workspace;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface WorkspaceRepository extends MongoRepository<Workspace, String> {

    String OWNER_FILTER = "{ 'ownerName' : ?#{principal?.username} }";
    String USERS_FILTER = "{ 'users' : ?#{principal?.username} }";
    String PRINCIPAL_IS_AUTHORIZED_FILTER = "{ $or: [" + OWNER_FILTER + ", " + USERS_FILTER + "] }";


    String SEQ_NUMBER_FILTER = "{ 'seqNumber' : :#{#seqNumber} }";

    @Query(PRINCIPAL_IS_AUTHORIZED_FILTER)
    List<Workspace> findAll();

    @Query(value = "{ $and: [" + PRINCIPAL_IS_AUTHORIZED_FILTER + ", " + SEQ_NUMBER_FILTER + "] }")
    Optional<Workspace> findBySeqNumber(@Param("seqNumber") int seqNumber);

    @Query(value = "{ $and: [" + PRINCIPAL_IS_AUTHORIZED_FILTER + ", " + SEQ_NUMBER_FILTER + "] }", exists = true)
    boolean existsBySeqNumber(@Param("seqNumber") int seqNumber);

    Optional<Workspace> findTopByOrderBySeqNumberDesc();

    @Query("{}")
    Stream<Workspace> _getAll();

    @Query(SEQ_NUMBER_FILTER)
    Optional<Workspace> _getBySeqNumber(@Param("seqNumber") int seqNumber);

}
