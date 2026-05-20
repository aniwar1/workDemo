package com.example.kgplatform.repository;

import com.example.kgplatform.entity.Neo4jEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Neo4jEntityRepository extends Neo4jRepository<Neo4jEntity, Long> {

    List<Neo4jEntity> findByGraphId(Long graphId);

    List<Neo4jEntity> findByGraphIdAndEntityType(Long graphId, String entityType);

    List<Neo4jEntity> findByGraphIdAndNameContaining(Long graphId, String keyword);

    @Query("MATCH (e:Entity {graphId: $graphId}) RETURN e")
    List<Neo4jEntity> findAllByGraphId(@Param("graphId") Long graphId);

    @Query("MATCH (e:Entity {graphId: $graphId, entityType: $entityType}) RETURN e")
    List<Neo4jEntity> findByGraphIdAndType(@Param("graphId") Long graphId, @Param("entityType") String entityType);

    long countByGraphId(Long graphId);
}
