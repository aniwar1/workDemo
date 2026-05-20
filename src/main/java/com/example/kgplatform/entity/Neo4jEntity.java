package com.example.kgplatform.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.Map;

@Node("Entity")
public class Neo4jEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("entityType")
    private String entityType;

    @Property("graphId")
    private Long graphId;

    @Property("properties")
    private Map<String, Object> properties;

    public Neo4jEntity() {}

    public Neo4jEntity(String name, String entityType, Long graphId, Map<String, Object> properties) {
        this.name = name;
        this.entityType = entityType;
        this.graphId = graphId;
        this.properties = properties;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getGraphId() { return graphId; }
    public void setGraphId(Long graphId) { this.graphId = graphId; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
}
