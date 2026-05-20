package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgAnnotationRecord;
import com.example.kgplatform.mapper.KgAnnotationRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class KgAnnotationRecordService extends ServiceImpl<KgAnnotationRecordMapper, KgAnnotationRecord> {

    public PageResult<KgAnnotationRecord> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQueryWithKeyword(this, query,
                (wrapper, kw) -> wrapper.like(KgAnnotationRecord::getContent, kw));
    }
}
