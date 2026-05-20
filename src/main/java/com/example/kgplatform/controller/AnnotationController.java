package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgAnnotationTask;
import com.example.kgplatform.entity.KgAnnotationRecord;
import com.example.kgplatform.mapper.KgAnnotationTaskMapper;
import com.example.kgplatform.mapper.KgAnnotationRecordMapper;
import com.example.kgplatform.service.KgAnnotationTaskService;
import com.example.kgplatform.service.KgAnnotationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "标注管理")
@RestController
@RequestMapping("/api/annotation")
public class AnnotationController {

    private final KgAnnotationTaskService taskService;
    private final KgAnnotationRecordService recordService;

    public AnnotationController(KgAnnotationTaskService taskService, KgAnnotationRecordService recordService) {
        this.taskService = taskService;
        this.recordService = recordService;
    }

    @Operation(summary = "分页查询标注任务")
    @GetMapping("/task/list")
    public R<PageResult<KgAnnotationTask>> taskList(PageQuery query) {
        return R.ok(taskService.pageQuery(query));
    }

    @Operation(summary = "创建标注任务")
    @PostMapping("/task")
    public R<Void> createTask(@RequestBody KgAnnotationTask task) {
        task.setStatus("pending");
        task.setCompletedCount(0);
        taskService.save(task);
        return R.ok();
    }

    @Operation(summary = "分配标注任务")
    @PostMapping("/task/{id}/assign")
    public R<Void> assignTask(@PathVariable Long id, @RequestParam Long assigneeId) {
        KgAnnotationTask task = taskService.getById(id);
        if (task != null) {
            task.setAssigneeId(assigneeId);
            task.setStatus("assigned");
            taskService.updateById(task);
        }
        return R.ok();
    }

    @Operation(summary = "分页查询标注记录")
    @GetMapping("/record/list")
    public R<PageResult<KgAnnotationRecord>> recordList(PageQuery query) {
        return R.ok(recordService.pageQuery(query));
    }

    @Operation(summary = "提交标注")
    @PostMapping("/record")
    public R<Void> saveRecord(@RequestBody KgAnnotationRecord record) {
        record.setStatus("completed");
        recordService.save(record);
        return R.ok();
    }

    @Operation(summary = "获取待标注数据")
    @GetMapping("/task/{id}/next")
    public R<KgAnnotationRecord> getNextRecord(@PathVariable Long id) {
        return R.ok(new KgAnnotationRecord());
    }

    @Operation(summary = "删除标注任务")
    @DeleteMapping("/task/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        taskService.removeById(id);
        return R.ok();
    }
}
