package com.example.kgplatform.repository;

import com.example.kgplatform.entity.Neo4jEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface Neo4jRelationRepository extends Neo4jRepository<Neo4jEntity, Long> {

    @Query("MATCH (s:Entity)-[r]->(t:Entity) WHERE s.graphId = $graphId RETURN s, r, t")
    List<Map<String, Object>> findAllRelationsByGraphId(@Param("graphId") Long graphId);

    @Query("MATCH (s:Entity {graphId: $graphId})-[r {relationType: $relationType}]->(t:Entity {graphId: $graphId}) RETURN s, r, t")
    List<Map<String, Object>> findByGraphIdAndRelationType(
            @Param("graphId") Long graphId,
            @Param("relationType") String relationType);

    long countByGraphId(Long graphId);
}
