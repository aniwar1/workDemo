package com.example.kgplatform.security;

import cn.hutool.core.util.StrUtil;
import com.example.kgplatform.config.JwtConfig;
import com.example.kgplatform.entity.SysRole;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.service.SysRoleService;
import com.example.kgplatform.service.SysUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    public JwtAuthFilter(JwtConfig jwtConfig, @Lazy SysUserService sysUserService, @Lazy SysRoleService sysRoleService) {
        this.jwtConfig = jwtConfig;
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(jwtConfig.getHeader());

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(jwtConfig.getTokenPrefix() + " ")) {
            String token = authHeader.substring(jwtConfig.getTokenPrefix().length() + 1);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);

                if (StrUtil.isNotBlank(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SysUser user = sysUserService.getByUsername(username);
                    if (user != null) {
                        String roleCode = "USER";
                        if (user.getRoleId() != null) {
                            SysRole role = sysRoleService.getById(user.getRoleId());
                            if (role != null) {
                                roleCode = role.getRoleCode();
                            }
                        }
                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + roleCode)
                        );
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);
                        authentication.setDetails(userId);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
