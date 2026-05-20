package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.mapper.KgExtractTaskMapper;
import com.example.kgplatform.service.KgExtractTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "知识抽取管理")
@RestController
@RequestMapping("/api/extract")
public class ExtractController {

    private final KgExtractTaskService kgExtractTaskService;

    public ExtractController(KgExtractTaskService kgExtractTaskService) {
        this.kgExtractTaskService = kgExtractTaskService;
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
        kgExtractTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "创建LLM抽取任务")
    @PostMapping("/llm")
    public R<Void> createLlmExtract(@RequestBody KgExtractTask task) {
        task.setExtractType("llm");
        task.setStatus("pending");
        kgExtractTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "启动抽取")
    @PostMapping("/{id}/start")
    public R<Void> startExtract(@PathVariable Long id) {
        KgExtractTask task = kgExtractTaskService.getById(id);
        if (task != null) {
            task.setStatus("running");
            kgExtractTaskService.updateById(task);
        }
        return R.ok();
    }

    @Operation(summary = "获取抽取结果")
    @GetMapping("/{id}/result")
    public R<KgExtractTask> getResult(@PathVariable Long id) {
        return R.ok(kgExtractTaskService.getById(id));
    }

    @Operation(summary = "删除抽取任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgExtractTaskService.removeById(id);
        return R.ok();
    }
}
