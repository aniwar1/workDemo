package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgAnnotationAuth;
import com.example.kgplatform.entity.KgAnnotationTask;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.service.KgAnnotationAuthService;
import com.example.kgplatform.service.KgAnnotationTaskService;
import com.example.kgplatform.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "标注授权")
@RestController
@RequestMapping("/api/annotation/auth")
public class AnnotationAuthController {

    private final KgAnnotationAuthService authService;
    private final KgAnnotationTaskService taskService;
    private final SysUserService userService;

    public AnnotationAuthController(KgAnnotationAuthService authService,
                                   KgAnnotationTaskService taskService,
                                   SysUserService userService) {
        this.authService = authService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @Operation(summary = "分页查询标注授权记录")
    @GetMapping("/list")
    public R<PageResult<KgAnnotationAuth>> list(PageQuery query) {
        return R.ok(authService.pageQuery(query));
    }

    @Operation(summary = "获取授权详情")
    @GetMapping("/{id}")
    public R<KgAnnotationAuth> getById(@PathVariable Long id) {
        return R.ok(authService.getById(id));
    }

    @Operation(summary = "新建标注授权")
    @PostMapping
    public R<Void> create(@RequestBody KgAnnotationAuth auth) {
        if (auth.getName() == null || auth.getName().isBlank()) {
            return R.fail("授权名称不能为空");
        }
        if (auth.getAnnotatorId() == null) {
            return R.fail("请选择标注人员");
        }
        SysUser annotator = userService.getById(auth.getAnnotatorId());
        if (annotator == null) {
            return R.fail("标注人员不存在");
        }
        auth.setAnnotatorUsername(annotator.getUsername());
        auth.setStatus("active");
        auth.setCreateTime(LocalDateTime.now());
        auth.setUpdateTime(LocalDateTime.now());
        authService.save(auth);

        KgAnnotationTask task = new KgAnnotationTask();
        task.setName(auth.getName());
        task.setCorpusId(auth.getCorpusId());
        task.setAssigneeId(auth.getAnnotatorId());
        task.setStatus("assigned");
        task.setAnnotationType(auth.getAnnotationType() != null ? auth.getAnnotationType() : "entity");
        task.setTotalCount(0);
        task.setCompletedCount(0);
        taskService.save(task);

        return R.ok();
    }

    @Operation(summary = "修改标注授权")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody KgAnnotationAuth auth) {
        KgAnnotationAuth existing = authService.getById(id);
        if (existing == null) {
            return R.fail("授权记录不存在");
        }
        auth.setId(id);
        auth.setUpdateTime(LocalDateTime.now());
        authService.updateById(auth);

        LambdaQueryWrapper<KgAnnotationTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KgAnnotationTask::getName, existing.getName())
               .eq(KgAnnotationTask::getAssigneeId, existing.getAnnotatorId())
               .last("LIMIT 1");
        KgAnnotationTask task = taskService.getOne(wrapper);
        if (task != null) {
            if (auth.getAnnotatorId() != null) {
                task.setAssigneeId(auth.getAnnotatorId());
            }
            if (auth.getName() != null) {
                task.setName(auth.getName());
            }
            taskService.updateById(task);
        }

        return R.ok();
    }

    @Operation(summary = "删除标注授权")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        KgAnnotationAuth auth = authService.getById(id);
        if (auth != null) {
            LambdaQueryWrapper<KgAnnotationTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KgAnnotationTask::getName, auth.getName())
                   .eq(KgAnnotationTask::getAssigneeId, auth.getAnnotatorId());
            taskService.remove(wrapper);
        }
        authService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "撤销标注授权（禁用）")
    @PostMapping("/revoke/{id}")
    public R<Void> revoke(@PathVariable Long id) {
        KgAnnotationAuth auth = authService.getById(id);
        if (auth == null) {
            return R.fail("授权记录不存在");
        }
        auth.setStatus("revoked");
        auth.setUpdateTime(LocalDateTime.now());
        authService.updateById(auth);
        return R.ok();
    }

    @Operation(summary = "恢复标注授权")
    @PostMapping("/activate/{id}")
    public R<Void> activate(@PathVariable Long id) {
        KgAnnotationAuth auth = authService.getById(id);
        if (auth == null) {
            return R.fail("授权记录不存在");
        }
        auth.setStatus("active");
        auth.setUpdateTime(LocalDateTime.now());
        authService.updateById(auth);
        return R.ok();
    }

    @Operation(summary = "获取可分配的标注人员列表")
    @GetMapping("/annotators")
    public R<List<SysUser>> listAnnotators() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1);
        return R.ok(userService.list(wrapper));
    }
}
