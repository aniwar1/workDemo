package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final PasswordEncoder passwordEncoder;

    public SysUserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    public SysUser getById(Long id) {
        return super.getById(id);
    }

    public PageResult<SysUser> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQuery(this, query, wrapper -> {
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                wrapper.like(SysUser::getUsername, query.getKeyword())
                       .or()
                       .like(SysUser::getNickname, query.getKeyword());
            }
        });
    }

    public boolean register(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        return save(user);
    }

    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) return false;
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    public boolean adminUpdatePassword(Long targetUserId, String newPassword) {
        SysUser user = getById(targetUserId);
        if (user == null) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }
}
