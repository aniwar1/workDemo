package com.example.kgplatform.service;

import com.example.kgplatform.entity.Neo4jEntity;
import com.example.kgplatform.repository.Neo4jEntityRepository;
import com.example.kgplatform.repository.Neo4jRelationRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @Transactional
    public List<Neo4jEntity> saveEntities(List<Neo4jEntity> entities) {
        return entityRepository.saveAll(entities);
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

    public List<Map<String, Object>> getGraphNodesAndEdges(Long graphId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Session session = driver.session()) {
            String nodeCypher = "MATCH (e:Entity {graphId: $graphId}) RETURN e";
            var nodeResult = session.run(nodeCypher, Map.of("graphId", graphId));
            List<Map<String, Object>> nodes = new ArrayList<>();
            while (nodeResult.hasNext()) {
                var node = nodeResult.next().get("e").asNode();
                Map<String, Object> nodeMap = new HashMap<>();
                nodeMap.put("id", node.id());
                nodeMap.put("name", node.get("name").asString());
                nodeMap.put("entityType", node.get("entityType").asString());
                nodeMap.put("graphId", node.get("graphId").asLong());
                if (node.containsKey("properties")) {
                    nodeMap.put("properties", node.get("properties").asMap());
                }
                nodes.add(nodeMap);
            }
            result.add(Map.of("nodes", nodes));

            String edgeCypher = """
                MATCH (s:Entity {graphId: $graphId})-[r]->(t:Entity {graphId: $graphId})
                RETURN id(s) as sourceId, id(t) as targetId, r.relationType as relationType
                """;
            var edgeResult = session.run(edgeCypher, Map.of("graphId", graphId));
            List<Map<String, Object>> edges = new ArrayList<>();
            while (edgeResult.hasNext()) {
                var record = edgeResult.next();
                Map<String, Object> edgeMap = new HashMap<>();
                edgeMap.put("sourceId", record.get("sourceId").asLong());
                edgeMap.put("targetId", record.get("targetId").asLong());
                edgeMap.put("relationType", record.get("relationType").asString());
                edges.add(edgeMap);
            }
            result.add(Map.of("edges", edges));
        }
        return result;
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
        return relationRepository.countByGraphId(graphId);
    }

    public Map<String, Object> getGraphStats(Long graphId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nodeCount", countEntities(graphId));
        stats.put("edgeCount", countRelations(graphId));
        return stats;
    }

    public List<Map<String, Object>> findShortestPath(Long startNodeId, Long endNodeId, Long graphId) {
        List<Map<String, Object>> path = new ArrayList<>();
        try (Session session = driver.session()) {
            String cypher = """
                MATCH path = shortestPath(
                    (start:Entity {graphId: $graphId})-[*]->(end:Entity {graphId: $graphId})
                )
                WHERE id(start) = $startId AND id(end) = $endId
                RETURN path
                """;
            var result = session.run(cypher, Map.of("startId", startNodeId, "endId", endNodeId, "graphId", graphId));
            if (result.hasNext()) {
                var pathValue = result.next().get("path");
                for (var node : pathValue.asPath().nodes()) {
                    Map<String, Object> nodeMap = new HashMap<>();
                    nodeMap.put("id", node.id());
                    nodeMap.put("name", node.get("name").asString());
                    nodeMap.put("entityType", node.get("entityType").asString());
                    path.add(nodeMap);
                }
            }
        } catch (Exception e) {
            return path;
        }
        return path;
    }

    public List<Map<String, Object>> findPathBetween(Long startId, Long endId, Long graphId) {
        List<Map<String, Object>> path = new ArrayList<>();
        try (Session session = driver.session()) {
            String cypher = """
                MATCH path = shortestPath(
                    (start:Entity)-[*]->(end:Entity)
                )
                WHERE id(start) = $startId AND id(end) = $endId AND start.graphId = $graphId AND end.graphId = $graphId
                RETURN path
                """;
            var result = session.run(cypher, Map.of("startId", startId, "endId", endId, "graphId", graphId));
            if (result.hasNext()) {
                var pathValue = result.next().get("path");
                for (var node : pathValue.asPath().nodes()) {
                    Map<String, Object> nodeMap = new HashMap<>();
                    nodeMap.put("id", node.id());
                    nodeMap.put("name", node.get("name").asString());
                    nodeMap.put("entityType", node.get("entityType").asString());
                    path.add(nodeMap);
                }
            }
        } catch (Exception e) {
            return path;
        }
        return path;
    }

    public List<String> getRelationTypes(Long graphId) {
        try (Session session = driver.session()) {
            String cypher = """
                MATCH ()-[r {graphId: $graphId}]->()
                RETURN DISTINCT r.relationType as type
                """;
            var result = session.run(cypher, Map.of("graphId", graphId));
            List<String> types = new ArrayList<>();
            while (result.hasNext()) {
                types.add(result.next().get("type").asString());
            }
            return types;
        }
    }

    @Transactional
    public Map<String, Long> batchCreateNodesAndRelations(List<Map<String, Object>> nodes, List<Map<String, Object>> relations) {
        Map<String, Long> nameToNeo4jId = new HashMap<>();
        try (Session session = driver.session()) {
            for (Map<String, Object> node : nodes) {
                String name = (String) node.get("name");
                Long graphId = ((Number) node.get("graphId")).longValue();
                String entityType = (String) node.getOrDefault("entityType", "实体");
                String cypher = """
                    MERGE (e:Entity {name: $name, graphId: $graphId})
                    SET e.entityType = $entityType
                    RETURN id(e) as neo4jId
                    """;
                var result = session.run(cypher, Map.of(
                        "name", name,
                        "graphId", graphId,
                        "entityType", entityType
                ));
                if (result.hasNext()) {
                    nameToNeo4jId.put(name, result.next().get("neo4jId").asLong());
                }
            }
            for (Map<String, Object> rel : relations) {
                String sourceName = (String) rel.get("sourceName");
                String targetName = (String) rel.get("targetName");
                String relationType = (String) rel.get("relationType");
                Long graphId = ((Number) rel.get("graphId")).longValue();
                String cypher = """
                    MATCH (s:Entity {name: $sourceName, graphId: $graphId})
                    MATCH (t:Entity {name: $targetName, graphId: $graphId})
                    MERGE (s)-[r:RELATION {relationType: $relationType, graphId: $graphId}]->(t)
                    """;
                session.run(cypher, Map.of(
                        "sourceName", sourceName,
                        "targetName", targetName,
                        "relationType", relationType,
                        "graphId", graphId
                ));
            }
        }
        return nameToNeo4jId;
    }
}
