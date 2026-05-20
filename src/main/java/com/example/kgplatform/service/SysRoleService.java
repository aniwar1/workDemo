package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.SysRole;
import com.example.kgplatform.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    public PageResult<SysRole> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQuery(this, query, wrapper -> {
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                wrapper.like(SysRole::getRoleName, query.getKeyword())
                       .or()
                       .like(SysRole::getRoleCode, query.getKeyword());
            }
            wrapper.orderByAsc(SysRole::getSortOrder);
        });
    }

    public SysRole getByCode(String code) {
        return getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
    }
}
