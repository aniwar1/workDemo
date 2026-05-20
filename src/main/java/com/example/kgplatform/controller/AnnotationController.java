package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgAnnotationTask;
import com.example.kgplatform.entity.KgAnnotationRecord;
import com.example.kgplatform.service.KgAnnotationTaskService;
import com.example.kgplatform.service.KgAnnotationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        task.setTotalCount(0);
        taskService.save(task);
        return R.ok();
    }

    @Operation(summary = "修改标注任务")
    @PutMapping("/task/{id}")
    public R<Void> updateTask(@PathVariable Long id, @RequestBody KgAnnotationTask task) {
        task.setId(id);
        taskService.updateById(task);
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

        KgAnnotationTask task = taskService.getById(record.getTaskId());
        if (task != null) {
            task.setCompletedCount(task.getCompletedCount() + 1);
            if (task.getTotalCount() <= task.getCompletedCount()) {
                task.setStatus("completed");
            } else {
                task.setStatus("in_progress");
            }
            taskService.updateById(task);
        }
        return R.ok();
    }

    @Operation(summary = "获取待标注数据")
    @GetMapping("/task/{id}/next")
    public R<KgAnnotationRecord> getNextRecord(@PathVariable Long id) {
        LambdaQueryWrapper<KgAnnotationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KgAnnotationRecord::getTaskId, id)
               .eq(KgAnnotationRecord::getStatus, "pending")
               .last("LIMIT 1");
        KgAnnotationRecord record = recordService.getOne(wrapper);
        if (record == null) {
            return R.ok(new KgAnnotationRecord());
        }
        return R.ok(record);
    }

    @Operation(summary = "审核标注记录")
    @PostMapping("/record/{id}/review")
    public R<Void> reviewRecord(@PathVariable Long id, @RequestParam String action) {
        KgAnnotationRecord record = recordService.getById(id);
        if (record == null) {
            return R.fail("记录不存在");
        }
        if ("approve".equals(action)) {
            record.setStatus("approved");
        } else if ("reject".equals(action)) {
            record.setStatus("rejected");
            record.setStatus("pending");
        }
        recordService.updateById(record);
        return R.ok();
    }

    @Operation(summary = "删除标注任务")
    @DeleteMapping("/task/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        LambdaQueryWrapper<KgAnnotationRecord> wrapper = new LambdaQueryWrapper<KgAnnotationRecord>()
                .eq(KgAnnotationRecord::getTaskId, id);
        recordService.remove(wrapper);
        taskService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "批量生成标注记录")
    @PostMapping("/task/{id}/generate")
    public R<Void> generateRecords(@PathVariable Long id) {
        KgAnnotationTask task = taskService.getById(id);
        if (task == null) {
            return R.fail("任务不存在");
        }
        for (int i = 0; i < 10; i++) {
            KgAnnotationRecord record = new KgAnnotationRecord();
            record.setTaskId(id);
            record.setCorpusId(task.getCorpusId());
            record.setContent("待标注文本内容 #" + (i + 1));
            record.setStatus("pending");
            recordService.save(record);
        }
        task.setTotalCount(10);
        taskService.updateById(task);
        return R.ok();
    }
}
