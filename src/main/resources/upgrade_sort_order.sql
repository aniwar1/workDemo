-- 角色管理增强升级脚本
-- MySQL 8.0.29+ 支持 IF NOT EXISTS；如果遇到语法错误，请手动注释掉第2行，只执行第3行 ALTER

-- 1. 给 sys_role 表新增 sort_order 列（已存在的数据库执行此语句）
--    如果报错 "Duplicate column"，说明列已存在，忽略此行
ALTER TABLE sys_role
ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0 COMMENT 'Sort order for display' AFTER description;

-- 2. 为已有角色设置排序号
UPDATE sys_role SET sort_order = 1 WHERE role_code = 'ADMIN';
UPDATE sys_role SET sort_order = 2 WHERE role_code = 'OPERATOR';
UPDATE sys_role SET sort_order = 3 WHERE role_code = 'ANNOTATOR';

-- 3. 如果 sort_order 为 NULL 的其他角色，补一个合理值
UPDATE sys_role SET sort_order = 99 WHERE sort_order IS NULL;
