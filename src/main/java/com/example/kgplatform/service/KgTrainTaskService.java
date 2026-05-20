package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgTrainTask;
import com.example.kgplatform.mapper.KgTrainTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgTrainTaskService extends ServiceImpl<KgTrainTaskMapper, KgTrainTask> {

    public PageResult<KgTrainTask> pageQuery(PageQuery query) {
        Page<KgTrainTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgTrainTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgTrainTask::getName, query.getKeyword());
        }
        Page<KgTrainTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
