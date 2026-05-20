-- =========================================
-- KG Platform Database Schema
-- =========================================

CREATE DATABASE IF NOT EXISTS kg_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kg_platform;

-- ----------------------------
-- System User Table
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'User ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(255) NOT NULL COMMENT 'Password (BCrypt encrypted)',
    nickname VARCHAR(64) COMMENT 'Nickname',
    email VARCHAR(128) COMMENT 'Email',
    phone VARCHAR(32) COMMENT 'Phone number',
    avatar VARCHAR(512) COMMENT 'Avatar URL',
    role_id BIGINT COMMENT 'Role ID',
    status INT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted INT DEFAULT 0 COMMENT 'Soft delete: 0=not deleted, 1=deleted',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System User';

-- ----------------------------
-- System Role Table
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Role ID',
    role_name VARCHAR(64) NOT NULL COMMENT 'Role name',
    role_code VARCHAR(64) NOT NULL UNIQUE COMMENT 'Role code',
    description VARCHAR(255) COMMENT 'Description',
    sort_order INT DEFAULT 0 COMMENT 'Sort order for display',
    status INT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System Role';

-- ----------------------------
-- Knowledge Graph Table
-- ----------------------------
DROP TABLE IF EXISTS kg_graph;
CREATE TABLE kg_graph (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Graph ID',
    name VARCHAR(128) NOT NULL COMMENT 'Graph name',
    description TEXT COMMENT 'Description',
    project_manager VARCHAR(64) COMMENT 'Project manager',
    model_name VARCHAR(128) COMMENT 'Model name',
    status VARCHAR(32) DEFAULT '1' COMMENT 'Status: 1=active, 0=inactive',
    model_id BIGINT COMMENT 'Model ID',
    storage_engine VARCHAR(64) COMMENT 'Storage engine: nebula, janus, tugraph, etc.',
    storage_engine_configured TINYINT DEFAULT 0 COMMENT 'Storage engine configured: 0=no, 1=yes',
    graph_space_created TINYINT DEFAULT 0 COMMENT 'Graph space created: 0=no, 1=yes',
    node_count INT DEFAULT 0 COMMENT 'Node count',
    edge_count INT DEFAULT 0 COMMENT 'Edge count',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_model_id (model_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Knowledge Graph';

-- ----------------------------
-- Graph Model Table
-- ----------------------------
DROP TABLE IF EXISTS kg_model;
CREATE TABLE kg_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Model ID',
    name VARCHAR(128) NOT NULL COMMENT 'Model name',
    description TEXT COMMENT 'Description',
    schema_col JSON COMMENT 'Model schema definition',
    status VARCHAR(32) DEFAULT '1' COMMENT 'Status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Graph Model';

-- ----------------------------
-- Corpus Table
-- ----------------------------
DROP TABLE IF EXISTS kg_corpus;
CREATE TABLE kg_corpus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Corpus ID',
    name VARCHAR(128) NOT NULL COMMENT 'Corpus name',
    file_path VARCHAR(512) COMMENT 'File path',
    file_type VARCHAR(32) COMMENT 'File type',
    file_size BIGINT COMMENT 'File size in bytes',
    status VARCHAR(32) DEFAULT '1' COMMENT 'Status',
    graph_id BIGINT COMMENT 'Associated graph ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_graph_id (graph_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Corpus';

-- ----------------------------
-- Annotation Task Table
-- ----------------------------
DROP TABLE IF EXISTS kg_annotation_task;
CREATE TABLE kg_annotation_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Task ID',
    name VARCHAR(128) NOT NULL COMMENT 'Task name',
    corpus_id BIGINT COMMENT 'Corpus ID',
    assignee_id BIGINT COMMENT 'Assignee user ID',
    status VARCHAR(32) DEFAULT 'pending' COMMENT 'Status: pending, assigned, in_progress, completed',
    total_count INT DEFAULT 0 COMMENT 'Total records count',
    completed_count INT DEFAULT 0 COMMENT 'Completed count',
    annotation_type VARCHAR(32) DEFAULT 'entity' COMMENT 'Annotation type: entity, relation',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_corpus_id (corpus_id),
    INDEX idx_assignee_id (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Annotation Task';

-- ----------------------------
-- Annotation Record Table
-- ----------------------------
DROP TABLE IF EXISTS kg_annotation_record;
CREATE TABLE kg_annotation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Record ID',
    task_id BIGINT COMMENT 'Task ID',
    corpus_id BIGINT COMMENT 'Corpus ID',
    annotator_id BIGINT COMMENT 'Annotator user ID',
    content TEXT COMMENT 'Original content',
    annotation TEXT COMMENT 'Annotation result',
    entity_types JSON COMMENT 'Entity types',
    relation_types JSON COMMENT 'Relation types',
    status VARCHAR(32) DEFAULT 'pending' COMMENT 'Status: pending, completed',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_task_id (task_id),
    INDEX idx_corpus_id (corpus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Annotation Record';

-- ----------------------------
-- Train Task Table
-- ----------------------------
DROP TABLE IF EXISTS kg_train_task;
CREATE TABLE kg_train_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Task ID',
    name VARCHAR(128) NOT NULL COMMENT 'Task name',
    model_type VARCHAR(64) COMMENT 'Model type',
    corpus_id BIGINT COMMENT 'Training corpus ID',
    config JSON COMMENT 'Training config',
    status VARCHAR(32) DEFAULT 'pending' COMMENT 'Status: pending, running, completed, failed',
    accuracy FLOAT COMMENT 'Accuracy',
    precision_col FLOAT COMMENT 'Precision',
    recall FLOAT COMMENT 'Recall',
    f1_score FLOAT COMMENT 'F1 Score',
    train_time BIGINT COMMENT 'Training time in seconds',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Model Training Task';

-- ----------------------------
-- Extract Task Table
-- ----------------------------
DROP TABLE IF EXISTS kg_extract_task;
CREATE TABLE kg_extract_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Task ID',
    name VARCHAR(128) NOT NULL COMMENT 'Task name',
    extract_type VARCHAR(32) NOT NULL COMMENT 'Extract type: dl, llm',
    graph_id BIGINT COMMENT 'Target graph ID',
    model_id BIGINT COMMENT 'Model ID to use',
    source_type VARCHAR(32) COMMENT 'Source type: corpus, text',
    source_id BIGINT COMMENT 'Source ID',
    status VARCHAR(32) DEFAULT 'pending' COMMENT 'Status: pending, running, completed, failed',
    extracted_count INT DEFAULT 0 COMMENT 'Extracted record count',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_graph_id (graph_id),
    INDEX idx_extract_type (extract_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Knowledge Extract Task';

-- ----------------------------
-- Graph Node Instance Table
-- ----------------------------
DROP TABLE IF EXISTS kg_node_instance;
CREATE TABLE kg_node_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Node ID',
    graph_id BIGINT NOT NULL COMMENT 'Graph ID',
    node_name VARCHAR(128) NOT NULL COMMENT 'Node name',
    node_type VARCHAR(64) COMMENT 'Node type',
    properties JSON COMMENT 'Node properties',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_graph_id (graph_id),
    INDEX idx_node_type (node_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Graph Node Instance';

-- ----------------------------
-- Graph Edge Instance Table
-- ----------------------------
DROP TABLE IF EXISTS kg_edge_instance;
CREATE TABLE kg_edge_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Edge ID',
    graph_id BIGINT NOT NULL COMMENT 'Graph ID',
    source_node_id BIGINT NOT NULL COMMENT 'Source node ID',
    target_node_id BIGINT NOT NULL COMMENT 'Target node ID',
    relation_type VARCHAR(64) COMMENT 'Relation type',
    properties JSON COMMENT 'Edge properties',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_graph_id (graph_id),
    INDEX idx_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Graph Edge Instance';

-- ----------------------------
-- Data Transform Task Table
-- ----------------------------
DROP TABLE IF EXISTS kg_transform_task;
CREATE TABLE kg_transform_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Task ID',
    name VARCHAR(128) NOT NULL COMMENT 'Task name',
    source_type VARCHAR(32) COMMENT 'Source type: csv, json, xml, sql',
    target_format VARCHAR(32) COMMENT 'Target format: kg_json, rdf, owl',
    input_path VARCHAR(512) COMMENT 'Input file path',
    output_path VARCHAR(512) COMMENT 'Output file path',
    status VARCHAR(32) DEFAULT 'pending' COMMENT 'Status: pending, running, completed, failed',
    record_count INT DEFAULT 0 COMMENT 'Processed record count',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Data Transform Task';

-- =========================================
-- Initial Data
-- =========================================

-- Insert default roles
INSERT INTO sys_role (role_name, role_code, description, sort_order, status) VALUES
('领域专家',   'DOMAIN_EXPERT', '负责知识图谱领域的专业审核与指导', 1, 1),
('图谱用户',   'GRAPH_USER',    '使用知识图谱进行查询与分析',      2, 1),
('数据标注员', 'ANNOTATOR',     '对语料数据进行标注与管理',        3, 1),
('图谱负责人', 'GRAPH_OWNER',   '管理所在领域的知识图谱建设',      4, 1),
('系统管理员', 'ADMIN',         '系统全部权限',                    5, 1);

-- Insert default admin user
-- Note: admin user is created by DataInitializer at startup using BCrypt encoding (admin / admin123)
--       role_id is resolved dynamically from role_code='ADMIN' to avoid hardcoded ID issues

-- Insert sample graph models
INSERT INTO kg_model (name, description, schema_col, status) VALUES
('通用实体模型', '通用知识图谱实体模型，支持人物、机构、地点等实体', '{"entityTypes":["人物","机构","地点","事件","概念"]}', '1'),
('医学知识模型', '医学领域知识图谱模型', '{"entityTypes":["疾病","症状","药物","检查","手术"]}', '1');

-- Insert sample graphs
INSERT INTO kg_graph (name, description, project_manager, model_name, model_id, node_count, edge_count, status) VALUES
('示例图谱1', '演示用知识图谱', '张三', '通用实体模型', 1, 125, 340, '1'),
('医学知识图谱', '医学领域知识图谱', '李四', '医学知识模型', 2, 89, 215, '1');

-- Insert sample corpus
INSERT INTO kg_corpus (name, file_path, file_type, file_size, graph_id, status) VALUES
('样本语料1', '/data/corpus/sample1.txt', 'txt', 102400, 1, '1'),
('样本语料2', '/data/corpus/sample2.json', 'json', 51200, 1, '1');

-- =========================================
-- Idempotent Schema Migration
-- Safe to re-run on existing databases
-- =========================================

-- Helper: add column if not exists (avoids "Duplicate column" errors)
DELIMITER //

DROP PROCEDURE IF EXISTS add_column_if_not_exists//
CREATE PROCEDURE add_column_if_not_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def VARCHAR(256)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = tbl_name
      AND COLUMN_NAME = col_name;
    IF col_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

-- ---- kg_graph: add project_manager ----
CALL add_column_if_not_exists('kg_graph', 'project_manager', "VARCHAR(64) COMMENT 'Project manager'")//

DELIMITER ;
