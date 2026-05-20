package com.example.kgplatform.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class Neo4jRelation {

    @Id
    @GeneratedValue
    private Long id;

    @Property("relationType")
    private String relationType;

    @Property("graphId")
    private Long graphId;

    @Property("properties")
    private java.util.Map<String, Object> properties;

    @TargetNode
    private Neo4jEntity target;

    @TargetNode
    private Neo4jEntity source;

    public Neo4jRelation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public Long getGraphId() { return graphId; }
    public void setGraphId(Long graphId) { this.graphId = graphId; }
    public java.util.Map<String, Object> getProperties() { return properties; }
    public void setProperties(java.util.Map<String, Object> properties) { this.properties = properties; }
    public Neo4jEntity getTarget() { return target; }
    public void setTarget(Neo4jEntity target) { this.target = target; }
    public Neo4jEntity getSource() { return source; }
    public void setSource(Neo4jEntity source) { this.source = source; }
}
