package com.example.kgplatform.service;

import com.example.kgplatform.entity.Neo4jEntity;
import com.example.kgplatform.repository.Neo4jEntityRepository;
import com.example.kgplatform.repository.Neo4jRelationRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Neo4jService {

    private final Neo4jEntityRepository entityRepository;
    private final Neo4jRelationRepository relationRepository;
    private final Driver driver;

    public Neo4jService(Neo4jEntityRepository entityRepository,
                        Neo4jRelationRepository relationRepository,
                        Driver driver) {
        this.entityRepository = entityRepository;
        this.relationRepository = relationRepository;
        this.driver = driver;
    }

    @Transactional
    public Neo4jEntity saveEntity(Neo4jEntity entity) {
        return entityRepository.save(entity);
    }

    public List<Neo4jEntity> getEntitiesByGraphId(Long graphId) {
        return entityRepository.findByGraphId(graphId);
    }

    public Optional<Neo4jEntity> getEntityById(Long id) {
        return entityRepository.findById(id);
    }

    public List<Neo4jEntity> searchEntities(Long graphId, String keyword) {
        return entityRepository.findByGraphIdAndNameContaining(graphId, keyword);
    }

    public List<Neo4jEntity> getEntitiesByType(Long graphId, String entityType) {
        return entityRepository.findByGraphIdAndEntityType(graphId, entityType);
    }

    public long countEntities(Long graphId) {
        return entityRepository.countByGraphId(graphId);
    }

    @Transactional
    public void deleteEntity(Long id) {
        entityRepository.deleteById(id);
    }

    public List<Map<String, Object>> getGraphData(Long graphId) {
        try (Session session = driver.session()) {
            String cypher = """
                MATCH (s:Entity {graphId: $graphId})-[r]->(t:Entity {graphId: $graphId})
                RETURN s, r, t
                """;
            return session.run(cypher, Map.of("graphId", graphId))
                    .stream()
                    .map(record -> Map.<String, Object>of(
                            "source", record.get("s").asMap(),
                            "relation", record.get("r").asMap(),
                            "target", record.get("t").asMap()
                    ))
                    .toList();
        }
    }

    @Transactional
    public void createRelation(Long sourceId, Long targetId, String relationType, Long graphId) {
        try (Session session = driver.session()) {
            String cypher = """
                MATCH (s:Entity), (t:Entity)
                WHERE s.id = $sourceId AND t.id = $targetId
                CREATE (s)-[r:RELATION {relationType: $relationType, graphId: $graphId}]->(t)
                RETURN r
                """;
            session.run(cypher, Map.of("sourceId", sourceId, "targetId", targetId, "relationType", relationType, "graphId", graphId));
        }
    }

    @Transactional
    public void deleteByGraphId(Long graphId) {
        try (Session session = driver.session()) {
            String cypher = "MATCH (e:Entity {graphId: $graphId}) DETACH DELETE e";
            session.run(cypher, Map.of("graphId", graphId));
        }
    }

    public long countRelations(Long graphId) {
        try (Session session = driver.session()) {
            String cypher = """
                MATCH ()-[r {graphId: $graphId}]->()
                RETURN count(r) as cnt
                """;
            var result = session.run(cypher, Map.of("graphId", graphId));
            return result.single().get("cnt").asLong();
        }
    }
}
