package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgModel;
import com.example.kgplatform.mapper.KgModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgModelService extends ServiceImpl<KgModelMapper, KgModel> {

    public PageResult<KgModel> pageQuery(PageQuery query) {
        Page<KgModel> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgModel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgModel::getName, query.getKeyword());
        }
        Page<KgModel> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
