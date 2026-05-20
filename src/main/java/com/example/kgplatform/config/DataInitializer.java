package com.example.kgplatform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.kgplatform.entity.SysRole;
import com.example.kgplatform.entity.SysUser;
import com.example.kgplatform.mapper.SysRoleMapper;
import com.example.kgplatform.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        if (sysRoleMapper.selectCount(null) > 0) {
            return;
        }
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("Administrator");
            setRoleCode("ADMIN");
            setDescription("System administrator");
            setStatus(1);
        }});
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("Operator");
            setRoleCode("OPERATOR");
            setDescription("Knowledge graph operator");
            setStatus(1);
        }});
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("Annotator");
            setRoleCode("ANNOTATOR");
            setDescription("Data annotator");
            setStatus(1);
        }});
        log.info("Default roles initialized");
    }

    private void initAdminUser() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin");
        if (sysUserMapper.selectCount(wrapper) > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("Administrator");
        admin.setEmail("admin@example.com");
        admin.setRoleId(1L);
        admin.setStatus(1);
        sysUserMapper.insert(admin);
        log.info("Default admin user created: admin / admin123");
    }
}
