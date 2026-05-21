package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.dto.KosExtractRequest;
import com.example.kgplatform.dto.LlmExtractResult;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.service.DashScopeService;
import com.example.kgplatform.service.KgEdgeInstanceService;
import com.example.kgplatform.service.KgGraphService;
import com.example.kgplatform.service.KgNodeInstanceService;
import com.example.kgplatform.service.Neo4jService;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "基于KOS知识抽取")
@RestController
@RequestMapping("/api/data/kos")
public class KosExtractController {

    private final DashScopeService dashScopeService;
    private final Neo4jService neo4jService;
    private final KgNodeInstanceService nodeInstanceService;
    private final KgEdgeInstanceService edgeInstanceService;
    private final KgGraphService kgGraphService;

    public KosExtractController(DashScopeService dashScopeService, Neo4jService neo4jService,
                               KgNodeInstanceService nodeInstanceService,
                               KgEdgeInstanceService edgeInstanceService,
                               KgGraphService kgGraphService) {
        this.dashScopeService = dashScopeService;
        this.neo4jService = neo4jService;
        this.nodeInstanceService = nodeInstanceService;
        this.edgeInstanceService = edgeInstanceService;
        this.kgGraphService = kgGraphService;
    }

    @Operation(summary = "KOS知识抽取（仅抽取，不保存）")
    @PostMapping("/extract")
    public R<LlmExtractResult> kosExtract(@RequestBody KosExtractRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            return R.fail("文本内容不能为空");
        }
        LlmExtractResult result = dashScopeService.extractDirect(
                request.getText(),
                request.getExtractType() != null ? request.getExtractType() : "all",
                request.getLanguage() != null ? request.getLanguage() : "zh",
                request.getModel()
        );
        return R.ok(result);
    }

    @Operation(summary = "KOS知识抽取并保存到图谱")
    @PostMapping("/extract-save/{graphId}")
    public R<Map<String, Object>> kosExtractSave(
            @RequestBody KosExtractRequest request,
            @PathVariable Long graphId) {
        if (request.getText() == null || request.getText().isBlank()) {
            return R.fail("文本内容不能为空");
        }
        if (graphId == null) {
            return R.fail("必须指定目标图谱ID");
        }

        LlmExtractResult result = dashScopeService.extractDirect(
                request.getText(),
                request.getExtractType() != null ? request.getExtractType() : "all",
                request.getLanguage() != null ? request.getLanguage() : "zh",
                request.getModel()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("entities", result.getEntities() != null ? result.getEntities() : new ArrayList<>());
        response.put("relations", result.getRelations() != null ? result.getRelations() : new ArrayList<>());

        List<LlmExtractResult.Entity> entities = result.getEntities();
        List<LlmExtractResult.Relation> relations = result.getRelations();

        if (entities == null) entities = new ArrayList<>();
        if (relations == null) relations = new ArrayList<>();

        if (!entities.isEmpty() || !relations.isEmpty()) {
            List<Map<String, Object>> nodesToSave = new ArrayList<>();
            for (LlmExtractResult.Entity e : entities) {
                Map<String, Object> nodeMap = new HashMap<>();
                nodeMap.put("name", e.getName());
                nodeMap.put("entityType", e.getType() != null ? e.getType() : "实体");
                nodeMap.put("graphId", graphId);
                if (e.getAttributes() != null) {
                    nodeMap.put("properties", e.getAttributes());
                }
                nodesToSave.add(nodeMap);
            }

            List<Map<String, Object>> relationsToSave = new ArrayList<>();
            for (LlmExtractResult.Relation r : relations) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("sourceName", r.getSource());
                relMap.put("targetName", r.getTarget());
                relMap.put("relationType", r.getType() != null ? r.getType() : "关系");
                relMap.put("graphId", graphId);
                if (r.getAttributes() != null) {
                    relMap.put("properties", r.getAttributes());
                }
                relationsToSave.add(relMap);
            }

            Map<String, Long> nameToNeo4jId = neo4jService.batchCreateNodesAndRelations(nodesToSave, relationsToSave);

            for (Map<String, Object> node : nodesToSave) {
                String name = (String) node.get("name");
                node.put("neo4jId", nameToNeo4jId.get(name));
            }

            Map<Long, Long> nameToMySqlId = new HashMap<>();
            for (com.example.kgplatform.entity.KgNodeInstance n : nodeInstanceService.batchSaveWithNeo4jIds(nodesToSave, null)) {
                nameToMySqlId.put(n.getNeo4jId(), n.getId());
            }

            for (Map<String, Object> rel : relationsToSave) {
                Long srcNeo4jId = nameToNeo4jId.get(rel.get("sourceName"));
                Long tgtNeo4jId = nameToNeo4jId.get(rel.get("targetName"));
                if (srcNeo4jId != null && tgtNeo4jId != null) {
                    rel.put("sourceNodeId", nameToMySqlId.getOrDefault(srcNeo4jId, -1L));
                    rel.put("targetNodeId", nameToMySqlId.getOrDefault(tgtNeo4jId, -1L));
                }
            }

            edgeInstanceService.batchSave(relationsToSave, null);

            KgGraph graph = kgGraphService.getById(graphId);
            if (graph != null) {
                int currentNodes = graph.getNodeCount() != null ? graph.getNodeCount() : 0;
                int currentEdges = graph.getEdgeCount() != null ? graph.getEdgeCount() : 0;
                graph.setNodeCount(currentNodes + nodesToSave.size());
                graph.setEdgeCount(currentEdges + relationsToSave.size());
                kgGraphService.updateById(graph);
            }

            response.put("nodeCount", nodesToSave.size());
            response.put("edgeCount", relationsToSave.size());
            response.put("saved", true);
        } else {
            response.put("nodeCount", 0);
            response.put("edgeCount", 0);
            response.put("saved", false);
        }

        return R.ok(response);
    }

    @Operation(summary = "获取图谱列表（用于KOS抽取）")
    @GetMapping("/graph-list")
    public R<PageResult<KgGraph>> listGraphs(PageQuery query) {
        return R.ok(kgGraphService.pageQuery(query));
    }
}
