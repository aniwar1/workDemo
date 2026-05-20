package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.SysRole;
import com.example.kgplatform.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    public PageResult<SysRole> pageQuery(PageQuery query) {
        Page<SysRole> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysRole::getRoleName, query.getKeyword())
                   .or().like(SysRole::getRoleCode, query.getKeyword());
        }
        wrapper.orderByAsc(SysRole::getSortOrder);
        Page<SysRole> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    public SysRole getByCode(String code) {
        return getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
    }
}
