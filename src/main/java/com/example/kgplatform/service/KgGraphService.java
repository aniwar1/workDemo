package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.mapper.KgGraphMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgGraphService extends ServiceImpl<KgGraphMapper, KgGraph> {

    public PageResult<KgGraph> pageQuery(PageQuery query) {
        Page<KgGraph> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgGraph> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgGraph::getName, query.getKeyword());
        }
        Page<KgGraph> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
