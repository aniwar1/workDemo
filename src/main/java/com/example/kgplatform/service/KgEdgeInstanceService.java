package com.example.kgplatform.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.entity.KgEdgeInstance;
import com.example.kgplatform.mapper.KgEdgeInstanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class KgEdgeInstanceService extends ServiceImpl<KgEdgeInstanceMapper, KgEdgeInstance> {

    @Transactional
    public List<KgEdgeInstance> batchSave(List<Map<String, Object>> edgeMaps, Long sourceTaskId) {
        List<KgEdgeInstance> saved = new java.util.ArrayList<>();
        for (Map<String, Object> edgeMap : edgeMaps) {
            KgEdgeInstance edge = new KgEdgeInstance();
            edge.setGraphId(((Number) edgeMap.get("graphId")).longValue());
            edge.setSourceNodeId(((Number) edgeMap.get("sourceNodeId")).longValue());
            edge.setTargetNodeId(((Number) edgeMap.get("targetNodeId")).longValue());
            edge.setRelationType((String) edgeMap.get("relationType"));
            Object props = edgeMap.get("properties");
            if (props != null) {
                edge.setProperties(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(props).toString());
            }
            if (sourceTaskId != null) {
                edge.setSourceTaskId(sourceTaskId);
            }
            save(edge);
            saved.add(edge);
        }
        return saved;
    }
}
