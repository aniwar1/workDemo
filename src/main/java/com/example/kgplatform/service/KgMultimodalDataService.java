package com.example.kgplatform.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgMultimodalData;
import com.example.kgplatform.mapper.KgMultimodalDataMapper;
import org.springframework.stereotype.Service;

@Service
public class KgMultimodalDataService extends ServiceImpl<KgMultimodalDataMapper, KgMultimodalData> {

    public PageResult<KgMultimodalData> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQueryWithKeyword(this, query,
                (wrapper, kw) -> wrapper.like(KgMultimodalData::getDescription, kw));
    }
}
