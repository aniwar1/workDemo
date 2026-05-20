package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.dto.ExtractRequest;
import com.example.kgplatform.dto.LlmExtractResult;
import com.example.kgplatform.service.DashScopeService;
import com.example.kgplatform.service.KgEdgeInstanceService;
import com.example.kgplatform.service.KgNodeInstanceService;
import com.example.kgplatform.service.Neo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.service.KgExtractTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "知识抽取管理")
@RestController
@RequestMapping("/api/data/extract")
public class ExtractController {

    private final DashScopeService dashScopeService;
    private final Neo4jService neo4jService;
    private final KgNodeInstanceService nodeInstanceService;
    private final KgEdgeInstanceService edgeInstanceService;
    private final KgExtractTaskService extractTaskService;

    public ExtractController(DashScopeService dashScopeService, Neo4jService neo4jService,
                             KgNodeInstanceService nodeInstanceService,
                             KgEdgeInstanceService edgeInstanceService,
                             KgExtractTaskService extractTaskService) {
        this.dashScopeService = dashScopeService;
        this.neo4jService = neo4jService;
        this.nodeInstanceService = nodeInstanceService;
        this.edgeInstanceService = edgeInstanceService;
        this.extractTaskService = extractTaskService;
    }

    @Operation(summary = "直接抽取（无需创建任务）")
    @PostMapping("/direct")
    public R<LlmExtractResult> extractDirect(@RequestBody ExtractRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            return R.fail("文本内容不能为空");
        }
        LlmExtractResult result = dashScopeService.extractDirect(
                request.getText(),
                request.getExtractType(),
                request.getLanguage(),
                request.getModel()
        );
        return R.ok(result);
    }

    @Operation(summary = "直接抽取并保存到图谱（双写 Neo4j + MySQL）")
    @PostMapping("/direct-save")
    public R<Map<String, Object>> extractDirectSave(@RequestBody ExtractRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            return R.fail("文本内容不能为空");
        }
        if (request.getGraphId() == null) {
            return R.fail("保存到图谱时必须指定 graphId");
        }

        LlmExtractResult result = dashScopeService.extractDirect(
                request.getText(),
                request.getExtractType(),
                request.getLanguage(),
                request.getModel()
        );

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("llmResult", result);

        List<LlmExtractResult.Entity> entities = result.getEntities();
        List<LlmExtractResult.Relation> relations = result.getRelations();

        List<Map<String, Object>> nodesToSave = new java.util.ArrayList<>();
        for (LlmExtractResult.Entity e : entities) {
            nodesToSave.add(Map.of(
                    "name", e.getName(),
                    "entityType", e.getType() != null ? e.getType() : "实体",
                    "graphId", request.getGraphId()
            ));
        }

        List<Map<String, Object>> relationsToSave = new java.util.ArrayList<>();
        for (LlmExtractResult.Relation r : relations) {
            relationsToSave.add(Map.of(
                    "sourceName", r.getSource(),
                    "targetName", r.getTarget(),
                    "relationType", r.getType() != null ? r.getType() : "关系",
                    "graphId", request.getGraphId()
            ));
        }

        if (!nodesToSave.isEmpty() || !relationsToSave.isEmpty()) {
            Map<String, Long> nameToNeo4jId = neo4jService.batchCreateNodesAndRelations(nodesToSave, relationsToSave);

            for (Map<String, Object> node : nodesToSave) {
                String name = (String) node.get("name");
                node.put("neo4jId", nameToNeo4jId.get(name));
            }

            Map<Long, Long> nameToMySqlId = new java.util.HashMap<>();
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

            response.put("nodeCount", nodesToSave.size());
            response.put("edgeCount", relationsToSave.size());
        } else {
            response.put("nodeCount", 0);
            response.put("edgeCount", 0);
        }

        return R.ok(response);
    }

    // ==================== 深度学习知识抽取任务管理 ====================

    @Operation(summary = "分页查询抽取任务")
    @GetMapping("/task/list")
    public R<PageResult<KgExtractTask>> listExtractTasks(PageQuery query) {
        return R.ok(extractTaskService.pageQuery(query));
    }

    @Operation(summary = "创建抽取任务")
    @PostMapping("/task")
    public R<Void> createExtractTask(@RequestBody KgExtractTask task) {
        task.setStatus("pending");
        task.setExtractedCount(0);
        extractTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "启动抽取任务")
    @PostMapping("/task/{id}/start")
    public R<Void> startExtractTask(@PathVariable Long id) {
        extractTaskService.startExtractAsync(id);
        return R.ok();
    }

    @Operation(summary = "停止抽取任务")
    @PostMapping("/task/{id}/stop")
    public R<Void> stopExtractTask(@PathVariable Long id) {
        extractTaskService.stopExtract(id);
        return R.ok();
    }

    @Operation(summary = "获取抽取任务结果")
    @GetMapping("/task/{id}/result")
    public R<Map<String, Object>> getExtractTaskResult(@PathVariable Long id) {
        Map<String, Object> result = extractTaskService.getExtractResult(id);
        if (result == null) {
            return R.fail("任务不存在");
        }
        return R.ok(result);
    }

    @Operation(summary = "删除抽取任务")
    @DeleteMapping("/task/{id}")
    public R<Void> deleteExtractTask(@PathVariable Long id) {
        extractTaskService.removeById(id);
        return R.ok();
    }
}
