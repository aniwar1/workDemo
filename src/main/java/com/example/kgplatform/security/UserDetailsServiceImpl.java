package com.example.kgplatform.security;

import com.example.kgplatform.entity.SysRole;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.service.SysRoleService;
import com.example.kgplatform.service.SysUserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;

    public UserDetailsServiceImpl(SysUserService sysUserService, SysRoleService sysRoleService) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        String roleCode = "USER";
        if (user.getRoleId() != null) {
            SysRole role = sysRoleService.getById(user.getRoleId());
            if (role != null) {
                roleCode = role.getRoleCode();
            }
        }
        return new User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleCode))
        );
    }
}
