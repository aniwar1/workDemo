package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgTrainTask;
import com.example.kgplatform.mapper.KgTrainTaskMapper;
import com.example.kgplatform.service.KgTrainTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "模型训练管理")
@RestController
@RequestMapping("/api/train")
public class ModelTrainController extends ServiceImpl<KgTrainTaskMapper, KgTrainTask> {

    private final KgTrainTaskService kgTrainTaskService;

    public ModelTrainController(KgTrainTaskService kgTrainTaskService) {
        this.kgTrainTaskService = kgTrainTaskService;
    }

    @Operation(summary = "分页查询训练任务")
    @GetMapping("/list")
    public R<PageResult<KgTrainTask>> list(PageQuery query) {
        return R.ok(kgTrainTaskService.pageQuery(query));
    }

    @Operation(summary = "获取训练任务详情")
    @GetMapping("/{id}")
    public R<KgTrainTask> getById(@PathVariable Long id) {
        return R.ok(kgTrainTaskService.getById(id));
    }

    @Operation(summary = "创建训练任务")
    @PostMapping
    public R<Void> add(@RequestBody KgTrainTask task) {
        task.setStatus("pending");
        kgTrainTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "启动训练")
    @PostMapping("/{id}/start")
    public R<Void> startTrain(@PathVariable Long id) {
        KgTrainTask task = kgTrainTaskService.getById(id);
        if (task != null) {
            task.setStatus("running");
            kgTrainTaskService.updateById(task);
        }
        return R.ok();
    }

    @Operation(summary = "获取训练效果指标")
    @GetMapping("/{id}/metrics")
    public R<KgTrainTask> getMetrics(@PathVariable Long id) {
        return R.ok(kgTrainTaskService.getById(id));
    }

    @Operation(summary = "删除训练任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgTrainTaskService.removeById(id);
        return R.ok();
    }
}
