package com.example.kgplatform.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgAnnotationAuth;
import com.example.kgplatform.mapper.KgAnnotationAuthMapper;
import org.springframework.stereotype.Service;

@Service
public class KgAnnotationAuthService extends ServiceImpl<KgAnnotationAuthMapper, KgAnnotationAuth> {

    public PageResult<KgAnnotationAuth> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQueryWithKeyword(this, query,
                (wrapper, kw) -> wrapper.like(KgAnnotationAuth::getName, kw)
                        .or()
                        .like(KgAnnotationAuth::getAnnotatorUsername, kw));
    }
}
