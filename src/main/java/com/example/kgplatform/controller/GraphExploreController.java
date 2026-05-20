package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.entity.KgNodeInstance;
import com.example.kgplatform.entity.KgEdgeInstance;
import com.example.kgplatform.service.KgGraphService;
import com.example.kgplatform.service.KgNodeInstanceService;
import com.example.kgplatform.service.KgEdgeInstanceService;
import com.example.kgplatform.service.Neo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "图谱探索")
@RestController
@RequestMapping("/api/explore")
public class GraphExploreController {

    private final KgGraphService kgGraphService;
    private final KgNodeInstanceService kgNodeInstanceService;
    private final KgEdgeInstanceService kgEdgeInstanceService;
    private final Neo4jService neo4jService;

    public GraphExploreController(KgGraphService kgGraphService, KgNodeInstanceService kgNodeInstanceService,
                                   KgEdgeInstanceService kgEdgeInstanceService, Neo4jService neo4jService) {
        this.kgGraphService = kgGraphService;
        this.kgNodeInstanceService = kgNodeInstanceService;
        this.kgEdgeInstanceService = kgEdgeInstanceService;
        this.neo4jService = neo4jService;
    }

    @Operation(summary = "查询图谱节点和边")
    @GetMapping("/graph/{graphId}")
    public R<Map<String, Object>> exploreGraph(@PathVariable Long graphId,
                                                @RequestParam(required = false) String relationType) {
        Map<String, Object> result = new HashMap<>();

        List<KgNodeInstance> allNodes = kgNodeInstanceService.list(
                new LambdaQueryWrapper<KgNodeInstance>().eq(KgNodeInstance::getGraphId, graphId));
        List<KgEdgeInstance> allEdges = kgEdgeInstanceService.list(
                new LambdaQueryWrapper<KgEdgeInstance>().eq(KgEdgeInstance::getGraphId, graphId));

        if (relationType != null && !relationType.isEmpty()) {
            allEdges = allEdges.stream()
                    .filter(e -> relationType.equals(e.getRelationType()))
                    .collect(Collectors.toList());
            Set<Long> relatedNodeIds = new HashSet<>();
            allEdges.forEach(e -> {
                relatedNodeIds.add(e.getSourceNodeId());
                relatedNodeIds.add(e.getTargetNodeId());
            });
            allNodes = allNodes.stream()
                    .filter(n -> relatedNodeIds.contains(n.getId()))
                    .collect(Collectors.toList());
        }

        result.put("nodes", allNodes);
        result.put("edges", allEdges);
        result.put("totalNodeCount", kgNodeInstanceService.count(
                new LambdaQueryWrapper<KgNodeInstance>().eq(KgNodeInstance::getGraphId, graphId)));
        result.put("totalEdgeCount", kgEdgeInstanceService.count(
                new LambdaQueryWrapper<KgEdgeInstance>().eq(KgEdgeInstance::getGraphId, graphId)));
        return R.ok(result);
    }

    @Operation(summary = "搜索节点")
    @GetMapping("/node/search")
    public R<List<KgNodeInstance>> searchNodes(@RequestParam String keyword,
                                                @RequestParam(required = false) Long graphId) {
        LambdaQueryWrapper<KgNodeInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(KgNodeInstance::getNodeName, keyword);
        if (graphId != null) {
            wrapper.eq(KgNodeInstance::getGraphId, graphId);
        }
        return R.ok(kgNodeInstanceService.list(wrapper));
    }

    @Operation(summary = "获取节点详情")
    @GetMapping("/node/{id}")
    public R<KgNodeInstance> getNodeDetail(@PathVariable Long id) {
        return R.ok(kgNodeInstanceService.getById(id));
    }

    @Operation(summary = "获取图谱列表")
    @GetMapping("/graphs")
    public R<List<KgGraph>> listGraphs() {
        return R.ok(kgGraphService.list());
    }

    @Operation(summary = "图谱统计数据")
    @GetMapping("/stats/{graphId}")
    public R<Map<String, Object>> getGraphStats(@PathVariable Long graphId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nodeCount", kgNodeInstanceService.count(
                new LambdaQueryWrapper<KgNodeInstance>().eq(KgNodeInstance::getGraphId, graphId)));
        stats.put("edgeCount", kgEdgeInstanceService.count(
                new LambdaQueryWrapper<KgEdgeInstance>().eq(KgEdgeInstance::getGraphId, graphId)));
        stats.put("neo4jNodeCount", neo4jService.countEntities(graphId));
        stats.put("neo4jEdgeCount", neo4jService.countRelations(graphId));
        return R.ok(stats);
    }

    @Operation(summary = "路径分析")
    @PostMapping("/path")
    public R<List<Map<String, Object>>> findPath(@RequestBody Map<String, Object> params) {
        Long startId = ((Number) params.get("startId")).longValue();
        Long endId = ((Number) params.get("endId")).longValue();
        Long graphId = params.get("graphId") != null ? ((Number) params.get("graphId")).longValue() : null;
        if (graphId == null) {
            KgNodeInstance startNode = kgNodeInstanceService.getById(startId);
            if (startNode != null) {
                graphId = startNode.getGraphId();
            }
        }
        if (graphId == null) {
            return R.fail("无法确定图谱ID");
        }
        List<Map<String, Object>> path = neo4jService.findPathBetween(startId, endId, graphId);
        return R.ok(path);
    }

    @Operation(summary = "获取关系类型列表")
    @GetMapping("/relation-types/{graphId}")
    public R<List<String>> getRelationTypes(@PathVariable Long graphId) {
        LambdaQueryWrapper<KgEdgeInstance> wrapper = new LambdaQueryWrapper<KgEdgeInstance>()
                .eq(KgEdgeInstance::getGraphId, graphId)
                .select(KgEdgeInstance::getRelationType);
        List<KgEdgeInstance> edges = kgEdgeInstanceService.list(wrapper);
        List<String> types = edges.stream()
                .map(KgEdgeInstance::getRelationType)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return R.ok(types);
    }
}
