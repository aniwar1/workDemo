package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.KgCorpus;
import com.example.kgplatform.mapper.KgCorpusMapper;
import com.example.kgplatform.service.KgCorpusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "语料管理")
@RestController
@RequestMapping("/api/corpus")
public class CorpusController extends ServiceImpl<KgCorpusMapper, KgCorpus> {

    private final KgCorpusService kgCorpusService;

    public CorpusController(KgCorpusService kgCorpusService) {
        this.kgCorpusService = kgCorpusService;
    }

    @Operation(summary = "分页查询语料")
    @GetMapping("/list")
    public R<PageResult<KgCorpus>> list(PageQuery query) {
        return R.ok(kgCorpusService.pageQuery(query));
    }

    @Operation(summary = "获取语料详情")
    @GetMapping("/{id}")
    public R<KgCorpus> getById(@PathVariable Long id) {
        return R.ok(kgCorpusService.getById(id));
    }

    @Operation(summary = "新增语料")
    @PostMapping
    public R<Void> add(@RequestBody KgCorpus corpus) {
        corpus.setStatus("1");
        kgCorpusService.save(corpus);
        return R.ok();
    }

    @Operation(summary = "修改语料")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody KgCorpus corpus) {
        corpus.setId(id);
        kgCorpusService.updateById(corpus);
        return R.ok();
    }

    @Operation(summary = "删除语料")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kgCorpusService.removeById(id);
        return R.ok();
    }
}
