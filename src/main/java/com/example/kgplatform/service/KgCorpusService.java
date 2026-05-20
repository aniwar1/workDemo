package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgCorpus;
import com.example.kgplatform.mapper.KgCorpusMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgCorpusService extends ServiceImpl<KgCorpusMapper, KgCorpus> {

    public PageResult<KgCorpus> pageQuery(PageQuery query) {
        Page<KgCorpus> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgCorpus> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgCorpus::getName, query.getKeyword());
        }
        Page<KgCorpus> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
