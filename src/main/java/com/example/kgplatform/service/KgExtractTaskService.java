package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.mapper.KgExtractTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgExtractTaskService extends ServiceImpl<KgExtractTaskMapper, KgExtractTask> {

    public PageResult<KgExtractTask> pageQuery(PageQuery query) {
        Page<KgExtractTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgExtractTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgExtractTask::getName, query.getKeyword());
        }
        Page<KgExtractTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
