package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.config.JwtConfig;
import com.example.kgplatform.dto.LoginDTO;
import com.example.kgplatform.dto.LoginVO;
import com.example.kgplatform.dto.PasswordDTO;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.service.SysUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    public AuthController(SysUserService sysUserService, PasswordEncoder passwordEncoder, JwtConfig jwtConfig) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        SysUser user = sysUserService.getByUsername(dto.getUsername());
        if (user == null) {
            return R.fail(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return R.fail(401, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            return R.fail(401, "账号已被禁用");
        }

        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                .compact();

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(token);
        vo.setRoleId(user.getRoleId());
        return R.ok("登录成功", vo);
    }

    @Operation(summary = "用户退出")
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<SysUser> getInfo(@RequestAttribute SysUser user) {
        return R.ok(user);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    public R<Void> changePassword(@RequestAttribute SysUser currentUser, @Valid @RequestBody PasswordDTO dto) {
        boolean ok = sysUserService.updatePassword(currentUser.getId(), dto.getOldPassword(), dto.getNewPassword());
        return ok ? R.ok() : R.fail("旧密码错误");
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody SysUser user) {
        if (sysUserService.getByUsername(user.getUsername()) != null) {
            return R.fail("用户名已存在");
        }
        boolean ok = sysUserService.register(user);
        return ok ? R.ok() : R.fail("注册失败");
    }
}
