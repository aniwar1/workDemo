package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgTransformTask;
import com.example.kgplatform.mapper.KgTransformTaskMapper;
import com.example.kgplatform.service.KgTransformTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "数据转化管理")
@RestController
@RequestMapping("/api/data/transform")
public class DataTransformController extends ServiceImpl<KgTransformTaskMapper, KgTransformTask> {

    private final KgTransformTaskService kgTransformTaskService;

    public DataTransformController(KgTransformTaskService kgTransformTaskService) {
        this.kgTransformTaskService = kgTransformTaskService;
    }

    @Operation(summary = "分页查询数据转换任务")
    @GetMapping("/list")
    public R<PageResult<KgTransformTask>> list(PageQuery query) {
        return R.ok(kgTransformTaskService.pageQuery(query));
    }

    @Operation(summary = "获取转换任务详情")
    @GetMapping("/{id}")
    public R<KgTransformTask> getById(@PathVariable Long id) {
        return R.ok(kgTransformTaskService.getById(id));
    }

    @Operation(summary = "新增转换任务")
    @PostMapping
    public R<Void> add(@RequestBody KgTransformTask task) {
        task.setStatus("pending");
        kgTransformTaskService.save(task);
        return R.ok();
    }

    @Operation(summary = "执行数据转换")
    @PostMapping("/{id}/execute")
    public R<Map<String, Object>> execute(@PathVariable Long id) {
        try {
            Map<String, Object> result = kgTransformTaskService.executeTransform(id);
            return R.ok(result);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @Operation(summary = "删除转换任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgTransformTaskService.removeById(id);
        return R.ok();
    }
}
