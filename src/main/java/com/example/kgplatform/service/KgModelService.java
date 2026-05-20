package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgModel;
import com.example.kgplatform.mapper.KgModelMapper;
import org.springframework.stereotype.Service;

@Service
public class KgModelService extends ServiceImpl<KgModelMapper, KgModel> {

    public PageResult<KgModel> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQueryWithKeyword(this, query,
                (wrapper, kw) -> wrapper.like(KgModel::getName, kw));
    }
}
