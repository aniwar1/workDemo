package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgAnnotationTask;
import com.example.kgplatform.mapper.KgAnnotationTaskMapper;
import org.springframework.stereotype.Service;

@Service
public class KgAnnotationTaskService extends ServiceImpl<KgAnnotationTaskMapper, KgAnnotationTask> {

    public PageResult<KgAnnotationTask> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQueryWithKeyword(this, query,
                (wrapper, kw) -> wrapper.like(KgAnnotationTask::getName, kw));
    }
}
