package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgAnnotationRecord;
import com.example.kgplatform.mapper.KgAnnotationRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KgAnnotationRecordService extends ServiceImpl<KgAnnotationRecordMapper, KgAnnotationRecord> {

    public PageResult<KgAnnotationRecord> pageQuery(PageQuery query) {
        Page<KgAnnotationRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgAnnotationRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgAnnotationRecord::getContent, query.getKeyword());
        }
        Page<KgAnnotationRecord> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }
}
