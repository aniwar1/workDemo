package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgTransformTask;
import com.example.kgplatform.mapper.KgTransformTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgTransformTaskService extends ServiceImpl<KgTransformTaskMapper, KgTransformTask> {

    public PageResult<KgTransformTask> pageQuery(PageQuery query) {
        Page<KgTransformTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgTransformTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgTransformTask::getName, query.getKeyword());
        }
        Page<KgTransformTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
