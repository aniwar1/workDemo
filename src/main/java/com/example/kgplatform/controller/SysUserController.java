package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.mapper.SysUserMapper;
import com.example.kgplatform.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    public SysUserController(SysUserService sysUserService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(PageQuery query) {
        return R.ok(sysUserService.pageQuery(query));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        return R.ok(sysUserService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public R<Void> add(@RequestBody SysUser user) {
        if (sysUserService.getByUsername(user.getUsername()) != null) {
            return R.fail("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setStatus(1);
        sysUserService.save(user);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        user.setPassword(null);
        user.setUsername(null);
        sysUserService.updateById(user);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return R.fail("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        sysUserService.updateById(user);
        return R.ok();
    }
}
