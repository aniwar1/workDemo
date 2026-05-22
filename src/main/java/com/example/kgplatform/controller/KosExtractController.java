package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.dto.KosExtractRequest;
import com.example.kgplatform.dto.LlmExtractResult;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.service.DashScopeService;
import com.example.kgplatform.service.KgEdgeInstanceService;
import com.example.kgplatform.service.KgGraphService;
import com.example.kgplatform.service.KgNodeInstanceService;
import com.example.kgplatform.service.Neo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "基于KOS知识抽取")
@RestController
@RequestMapping("/api/data/kos")
public class KosExtractController {

    private static final Logger log = LoggerFactory.getLogger(KosExtractController.class);

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
        log.info("[kosExtractSave] 开始处理，graphId={}, text长度={}, extractType={}, language={}, model={}, schema={}",
                graphId,
                request.getText() != null ? request.getText().length() : 0,
                request.getExtractType(),
                request.getLanguage(),
                request.getModel(),
                request.getSchema());

        if (request.getText() == null || request.getText().isBlank()) {
            return R.fail("文本内容不能为空");
        }
        if (graphId == null) {
            return R.fail("必须指定目标图谱ID");
        }

        LlmExtractResult result;
        try {
            result = dashScopeService.extractDirect(
                    request.getText(),
                    request.getExtractType() != null ? request.getExtractType() : "all",
                    request.getLanguage() != null ? request.getLanguage() : "zh",
                    request.getModel(),
                    request.getSchema()
            );
        } catch (Exception e) {
            log.error("[kosExtractSave] LLM抽取失败", e);
            return R.fail("LLM抽取失败: " + e.getMessage());
        }

        log.info("[kosExtractSave] LLM抽取完成，实体数={}, 关系数={}",
                result.getEntities() != null ? result.getEntities().size() : 0,
                result.getRelations() != null ? result.getRelations().size() : 0);

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
                String name = e.getName() != null ? e.getName().trim() : "未知";
                nodeMap.put("name", name);
                nodeMap.put("entityType", e.getType() != null ? e.getType().trim() : "实体");
                nodeMap.put("graphId", graphId);
                if (e.getAttributes() != null) {
                    nodeMap.put("properties", e.getAttributes());
                }
                nodesToSave.add(nodeMap);
            }

            List<Map<String, Object>> relationsToSave = new ArrayList<>();
            for (LlmExtractResult.Relation r : relations) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("sourceName", r.getSource() != null ? r.getSource().trim() : "");
                relMap.put("targetName", r.getTarget() != null ? r.getTarget().trim() : "");
                relMap.put("relationType", r.getType() != null ? r.getType().trim() : "关系");
                relMap.put("graphId", graphId);
                if (r.getAttributes() != null) {
                    relMap.put("properties", r.getAttributes());
                }
                relationsToSave.add(relMap);
            }

            log.info("[kosExtractSave] 准备保存到Neo4j，节点数={}, 关系数={}", nodesToSave.size(), relationsToSave.size());
            Map<String, Long> nameToNeo4jId = neo4jService.batchCreateNodesAndRelations(nodesToSave, relationsToSave);
            log.info("[kosExtractSave] Neo4j保存完成，成功创建的节点ID映射数={}", nameToNeo4jId.size());

            for (Map<String, Object> node : nodesToSave) {
                String name = (String) node.get("name");
                node.put("neo4jId", nameToNeo4jId.get(name));
            }

            Map<Long, Long> nameToMySqlId = new HashMap<>();
            for (com.example.kgplatform.entity.KgNodeInstance n : nodeInstanceService.batchSaveWithNeo4jIds(nodesToSave, null)) {
                nameToMySqlId.put(n.getNeo4jId(), n.getId());
            }
            log.info("[kosExtractSave] MySQL节点保存完成，数量={}", nameToMySqlId.size());

            int validRelCount = 0;
            for (Map<String, Object> rel : relationsToSave) {
                Long srcNeo4jId = nameToNeo4jId.get(rel.get("sourceName"));
                Long tgtNeo4jId = nameToNeo4jId.get(rel.get("targetName"));
                if (srcNeo4jId != null && tgtNeo4jId != null) {
                    rel.put("sourceNodeId", nameToMySqlId.getOrDefault(srcNeo4jId, -1L));
                    rel.put("targetNodeId", nameToMySqlId.getOrDefault(tgtNeo4jId, -1L));
                    validRelCount++;
                } else {
                    log.warn("[kosExtractSave] 关系跳过的源/目标节点未在Neo4j中找到，sourceName={}, srcNeo4jId={}, targetName={}, tgtNeo4jId={}",
                            rel.get("sourceName"), srcNeo4jId, rel.get("targetName"), tgtNeo4jId);
                }
            }
            log.info("[kosExtractSave] 有效关系数={}, 开始保存到MySQL", validRelCount);

            List<Map<String, Object>> validRelations = relationsToSave.stream()
                    .filter(rel -> {
                        Long srcNeo4jId = nameToNeo4jId.get(rel.get("sourceName"));
                        Long tgtNeo4jId = nameToNeo4jId.get(rel.get("targetName"));
                        return srcNeo4jId != null && tgtNeo4jId != null;
                    })
                    .toList();

            edgeInstanceService.batchSave(validRelations, null);
            log.info("[kosExtractSave] MySQL边保存完成");

            KgGraph graph = kgGraphService.getById(graphId);
            if (graph != null) {
                int currentNodes = graph.getNodeCount() != null ? graph.getNodeCount() : 0;
                int currentEdges = graph.getEdgeCount() != null ? graph.getEdgeCount() : 0;
                graph.setNodeCount(currentNodes + nodesToSave.size());
                graph.setEdgeCount(currentEdges + validRelCount);
                kgGraphService.updateById(graph);
                log.info("[kosExtractSave] 图谱计数更新，节点: {} -> {}, 边: {} -> {}",
                        currentNodes, graph.getNodeCount(), currentEdges, graph.getEdgeCount());
            }

            response.put("nodeCount", nodesToSave.size());
            response.put("edgeCount", validRelCount);
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

    @Operation(summary = "获取图谱节点和关系数据（用于图谱预览）")
    @GetMapping("/graph-data/{graphId}")
    public R<Map<String, Object>> getGraphData(@PathVariable Long graphId) {
        log.info("[getGraphData] 开始获取图谱数据，graphId={}", graphId);
        if (graphId == null) {
            return R.fail("图谱ID不能为空");
        }

        List<Map<String, Object>> graphData = neo4jService.getGraphData(graphId);
        log.info("[getGraphData] Neo4j原始查询结果记录数={}", graphData.size());

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Set<String> seenNodes = new HashSet<>();

        for (Map<String, Object> record : graphData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> source = (Map<String, Object>) record.get("source");
            @SuppressWarnings("unchecked")
            Map<String, Object> relation = record.get("relation") != null ? (Map<String, Object>) record.get("relation") : null;
            @SuppressWarnings("unchecked")
            Map<String, Object> target = record.get("target") != null ? (Map<String, Object>) record.get("target") : null;

            String srcName = extractNodeName(source);

            if (!seenNodes.contains(srcName)) {
                seenNodes.add(srcName);
                nodes.add(Map.of(
                        "name", srcName,
                        "type", source.get("entityType") != null ? source.get("entityType").toString() : "实体"
                ));
            }

            if (relation != null && target != null) {
                String tgtName = extractNodeName(target);
                if (!seenNodes.contains(tgtName)) {
                    seenNodes.add(tgtName);
                    nodes.add(Map.of(
                            "name", tgtName,
                            "type", target.get("entityType") != null ? target.get("entityType").toString() : "实体"
                    ));
                }

                edges.add(Map.of(
                        "source", srcName,
                        "target", tgtName,
                        "type", relation.get("relationType") != null ? relation.get("relationType").toString() : "关系"
                ));
            }
        }

        log.info("[getGraphData] 最终结果，节点数={}, 边数={}", nodes.size(), edges.size());

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("relations", edges);
        result.put("nodeCount", nodes.size());
        result.put("edgeCount", edges.size());
        return R.ok(result);
    }

    private String extractNodeName(Map<String, Object> node) {
        if (node == null) return "";
        Object name = node.get("name");
        return name != null ? name.toString() : "";
    }
}
