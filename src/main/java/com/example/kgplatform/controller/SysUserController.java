package com.example.kgplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.common.R;
import com.example.kgplatform.dto.AdminPasswordDTO;
import com.example.kgplatform.entity.SysRole;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.mapper.SysUserMapper;
import com.example.kgplatform.service.SysRoleService;
import com.example.kgplatform.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/user")
public class SysUserController extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final PasswordEncoder passwordEncoder;

    public SysUserController(SysUserService sysUserService, SysRoleService sysRoleService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/list")
    public R<PageResult<SysUser>> list(PageQuery query) {
        PageResult<SysUser> result = sysUserService.pageQuery(query);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            SysUser current = (SysUser) auth.getPrincipal();
            result.getList().removeIf(u -> !u.getId().equals(current.getId()));
            result.setTotal(result.getList().size());
        }
        return R.ok(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public R<SysUser> getById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SysUser current = (SysUser) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !current.getId().equals(id)) {
            return R.fail(403, "无权限访问该用户信息");
        }
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

    @Operation(summary = "管理员修改用户密码")
    @PostMapping("/{id}/password")
    public R<Void> adminUpdatePassword(@PathVariable Long id, @Valid @RequestBody AdminPasswordDTO dto) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return R.fail("用户不存在");
        if (!id.equals(dto.getTargetUserId())) return R.fail("参数不匹配");
        boolean ok = sysUserService.adminUpdatePassword(id, dto.getNewPassword());
        return ok ? R.ok() : R.fail("修改失败");
    }
}
