package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgAnnotationTask;
import com.example.kgplatform.mapper.KgAnnotationTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgAnnotationTaskService extends ServiceImpl<KgAnnotationTaskMapper, KgAnnotationTask> {

    public PageResult<KgAnnotationTask> pageQuery(PageQuery query) {
        Page<KgAnnotationTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgAnnotationTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgAnnotationTask::getName, query.getKeyword());
        }
        Page<KgAnnotationTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
