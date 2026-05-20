package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgModel;
import com.example.kgplatform.mapper.KgModelMapper;
import com.example.kgplatform.service.KgModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "图谱模型管理")
@RestController
@RequestMapping("/api/kg/model")
public class GraphModelController extends ServiceImpl<KgModelMapper, KgModel> {

    private final KgModelService kgModelService;

    public GraphModelController(KgModelService kgModelService) {
        this.kgModelService = kgModelService;
    }

    @Operation(summary = "分页查询图谱模型")
    @GetMapping("/list")
    public R<PageResult<KgModel>> list(PageQuery query) {
        return R.ok(kgModelService.pageQuery(query));
    }

    @Operation(summary = "获取模型详情")
    @GetMapping("/{id}")
    public R<KgModel> getById(@PathVariable Long id) {
        return R.ok(kgModelService.getById(id));
    }

    @Operation(summary = "新增模型")
    @PostMapping
    public R<Void> add(@RequestBody KgModel model) {
        model.setStatus(Integer.valueOf("1"));
        kgModelService.save(model);
        return R.ok();
    }

    @Operation(summary = "修改模型")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody KgModel model) {
        model.setId(id);
        kgModelService.updateById(model);
        return R.ok();
    }

    @Operation(summary = "删除模型")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgModelService.removeById(id);
        return R.ok();
    }
}
