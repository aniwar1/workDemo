package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.mapper.KgExtractTaskMapper;
import com.example.kgplatform.service.KgExtractTaskService;
import com.example.kgplatform.service.Neo4jService;
import com.example.kgplatform.service.KgCorpusService;
import com.example.kgplatform.service.DashScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "知识抽取管理")
@RestController
@RequestMapping("/api/extract")
public class ExtractController {

    private final KgExtractTaskService kgExtractTaskService;
    private final Neo4jService neo4jService;
    private final KgCorpusService corpusService;
    private final DashScopeService dashScopeService;

    public ExtractController(KgExtractTaskService kgExtractTaskService, Neo4jService neo4jService,
                             KgCorpusService corpusService, DashScopeService dashScopeService) {
        this.kgExtractTaskService = kgExtractTaskService;
        this.neo4jService = neo4jService;
        this.corpusService = corpusService;
        this.dashScopeService = dashScopeService;
    }

    @Operation(summary = "分页查询抽取任务")
    @GetMapping("/list")
    public R<PageResult<KgExtractTask>> list(PageQuery query) {
        return R.ok(kgExtractTaskService.pageQuery(query));
    }

    @Operation(summary = "创建深度学习抽取任务")
    @PostMapping("/dl")
    public R<Void> createDlExtract(@RequestBody KgExtractTask task) {
        task.setExtractType("dl");
        task.setStatus("pending");
        task.setExtractedCount(0);
        kgExtractTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "创建LLM抽取任务")
    @PostMapping("/llm")
    public R<Void> createLlmExtract(@RequestBody KgExtractTask task) {
        task.setExtractType("llm");
        task.setStatus("pending");
        task.setExtractedCount(0);
        kgExtractTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "启动抽取")
    @PostMapping("/{id}/start")
    public R<Void> startExtract(@PathVariable Long id) {
        KgExtractTask task = kgExtractTaskService.getById(id);
        if (task == null) {
            return R.fail("任务不存在");
        }
        if ("running".equals(task.getStatus())) {
            return R.fail("任务已在运行中");
        }
        kgExtractTaskService.startExtractAsync(id, neo4jService, corpusService, dashScopeService);
        return R.ok();
    }

    @Operation(summary = "停止抽取")
    @PostMapping("/{id}/stop")
    public R<Void> stopExtract(@PathVariable Long id) {
        kgExtractTaskService.stopExtract(id);
        return R.ok();
    }

    @Operation(summary = "获取抽取结果")
    @GetMapping("/{id}/result")
    public R<Map<String, Object>> getResult(@PathVariable Long id) {
        KgExtractTask task = kgExtractTaskService.getById(id);
        if (task == null) {
            return R.fail("任务不存在");
        }
        if (task.getGraphId() == null) {
            return R.ok(Map.of(
                    "task", task,
                    "nodes", List.of(),
                    "edges", List.of()
            ));
        }
        List<Map<String, Object>> graphData = neo4jService.getGraphData(task.getGraphId());
        return R.ok(Map.of(
                "task", task,
                "extractedCount", task.getExtractedCount(),
                "graphData", graphData
        ));
    }

    @Operation(summary = "删除抽取任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgExtractTaskService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "获取抽取任务详情")
    @GetMapping("/{id}")
    public R<KgExtractTask> getById(@PathVariable Long id) {
        return R.ok(kgExtractTaskService.getById(id));
    }
}
