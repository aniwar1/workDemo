package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.entity.KgNodeInstance;
import com.example.kgplatform.entity.KgEdgeInstance;
import com.example.kgplatform.service.KgGraphService;
import com.example.kgplatform.service.KgNodeInstanceService;
import com.example.kgplatform.service.KgEdgeInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "图谱探索")
@RestController
@RequestMapping("/api/explore")
public class GraphExploreController {

    private final KgGraphService kgGraphService;
    private final KgNodeInstanceService kgNodeInstanceService;
    private final KgEdgeInstanceService kgEdgeInstanceService;

    public GraphExploreController(KgGraphService kgGraphService, KgNodeInstanceService kgNodeInstanceService,
                                   KgEdgeInstanceService kgEdgeInstanceService) {
        this.kgGraphService = kgGraphService;
        this.kgNodeInstanceService = kgNodeInstanceService;
        this.kgEdgeInstanceService = kgEdgeInstanceService;
    }

    @Operation(summary = "查询图谱节点和边")
    @GetMapping("/graph/{graphId}")
    public R<Map<String, Object>> exploreGraph(@PathVariable Long graphId) {
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", kgNodeInstanceService.list());
        result.put("edges", kgEdgeInstanceService.list());
        return R.ok(result);
    }

    @Operation(summary = "搜索节点")
    @GetMapping("/node/search")
    public R<List<KgNodeInstance>> searchNodes(@RequestParam String keyword, @RequestParam(required = false) Long graphId) {
        return R.ok(kgNodeInstanceService.list());
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
}
