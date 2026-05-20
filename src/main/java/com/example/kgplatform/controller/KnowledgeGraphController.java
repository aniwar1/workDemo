package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.mapper.KgGraphMapper;
import com.example.kgplatform.service.KgGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识图谱管理")
@RestController
@RequestMapping("/api/kg/graph")
public class KnowledgeGraphController extends ServiceImpl<KgGraphMapper, KgGraph> {

    private final KgGraphService kgGraphService;

    public KnowledgeGraphController(KgGraphService kgGraphService) {
        this.kgGraphService = kgGraphService;
    }

    @Operation(summary = "分页查询知识图谱")
    @GetMapping("/list")
    public R<PageResult<KgGraph>> list(PageQuery query) {
        return R.ok(kgGraphService.pageQuery(query));
    }

    @Operation(summary = "获取知识图谱详情")
    @GetMapping("/{id}")
    public R<KgGraph> getById(@PathVariable Long id) {
        return R.ok(kgGraphService.getById(id));
    }

    @Operation(summary = "新增知识图谱")
    @PostMapping
    public R<Void> add(@RequestBody KgGraph graph) {
        graph.setStatus("1");
        kgGraphService.save(graph);
        return R.ok();
    }

    @Operation(summary = "修改知识图谱")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody KgGraph graph) {
        graph.setId(id);
        kgGraphService.updateById(graph);
        return R.ok();
    }

    @Operation(summary = "删除知识图谱")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgGraphService.removeById(id);
        return R.ok();
    }
}
