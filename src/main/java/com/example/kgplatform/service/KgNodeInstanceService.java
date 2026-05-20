package com.example.kgplatform.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.entity.KgNodeInstance;
import com.example.kgplatform.mapper.KgNodeInstanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class KgNodeInstanceService extends ServiceImpl<KgNodeInstanceMapper, KgNodeInstance> {

    @Transactional
    public List<KgNodeInstance> batchSaveWithNeo4jIds(List<Map<String, Object>> nodeMaps, Long sourceTaskId) {
        List<KgNodeInstance> saved = new java.util.ArrayList<>();
        for (Map<String, Object> nodeMap : nodeMaps) {
            KgNodeInstance node = new KgNodeInstance();
            node.setGraphId(((Number) nodeMap.get("graphId")).longValue());
            node.setNodeName((String) nodeMap.get("name"));
            node.setNodeType((String) nodeMap.getOrDefault("entityType", "实体"));
            Object props = nodeMap.get("properties");
            if (props != null) {
                node.setProperties(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(props).toString());
            }
            if (nodeMap.containsKey("neo4jId")) {
                node.setNeo4jId(((Number) nodeMap.get("neo4jId")).longValue());
            }
            if (sourceTaskId != null) {
                node.setSourceTaskId(sourceTaskId);
            }
            save(node);
            saved.add(node);
        }
        return saved;
    }
}
