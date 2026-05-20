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
            setRoleName("领域专家");
            setRoleCode("DOMAIN_EXPERT");
            setDescription("负责知识图谱领域的专业审核与指导");
            setSortOrder(1);
            setStatus(1);
        }});
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("图谱用户");
            setRoleCode("GRAPH_USER");
            setDescription("使用知识图谱进行查询与分析");
            setSortOrder(2);
            setStatus(1);
        }});
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("数据标注员");
            setRoleCode("ANNOTATOR");
            setDescription("对语料数据进行标注与管理");
            setSortOrder(3);
            setStatus(1);
        }});
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("图谱负责人");
            setRoleCode("GRAPH_OWNER");
            setDescription("管理所在领域的知识图谱建设");
            setSortOrder(4);
            setStatus(1);
        }});
        sysRoleMapper.insert(new SysRole() {{
            setRoleName("系统管理员");
            setRoleCode("ADMIN");
            setDescription("系统全部权限");
            setSortOrder(5);
            setStatus(1);
        }});
        log.info("Default roles initialized");
    }

    private void initAdminUser() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin");
        if (sysUserMapper.selectCount(wrapper) > 0) {
            return;
        }
        SysRole adminRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "ADMIN"));
        if (adminRole == null) {
            log.warn("ADMIN role not found, skip creating admin user");
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("Administrator");
        admin.setEmail("admin@example.com");
        admin.setRoleId(adminRole.getId());
        admin.setStatus(1);
        sysUserMapper.insert(admin);
        log.info("Default admin user created: admin / admin123");
    }
}
