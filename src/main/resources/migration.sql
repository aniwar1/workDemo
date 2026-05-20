-- =========================================
-- KG Platform Schema Migration (Idempotent)
-- Safe to run on existing databases
-- Adds missing columns introduced by entity changes
-- =========================================

-- Helper: add column if not exists
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

-- ---- kg_graph: project_manager ----
-- Required by: KgGraph.projectManager
-- Type: VARCHAR(64)
CALL add_column_if_not_exists('kg_graph', 'project_manager', "VARCHAR(64) COMMENT 'Project manager'")//

-- ---- kg_graph: model_name ----
-- Required by: KgGraph.modelName
-- Type: VARCHAR(128)
CALL add_column_if_not_exists('kg_graph', 'model_name', "VARCHAR(128) COMMENT 'Model name'")//

-- ---- kg_graph: storage_engine ----
-- Required by: KgGraph.storageEngine
-- Type: VARCHAR(64)
CALL add_column_if_not_exists('kg_graph', 'storage_engine', "VARCHAR(64) COMMENT 'Storage engine: nebula, janus, tugraph, etc.'")//

-- ---- kg_graph: storage_engine_configured ----
-- Required by: KgGraph.storageEngineConfigured
-- Type: TINYINT
CALL add_column_if_not_exists('kg_graph', 'storage_engine_configured', "TINYINT DEFAULT 0 COMMENT 'Storage engine configured: 0=no, 1=yes'")//

-- ---- kg_graph: graph_space_created ----
-- Required by: KgGraph.graphSpaceCreated
-- Type: TINYINT
CALL add_column_if_not_exists('kg_graph', 'graph_space_created', "TINYINT DEFAULT 0 COMMENT 'Graph space created: 0=no, 1=yes'")//

-- ---- kg_transform_task: graph_id ----
-- Required by: KgTransformTask.graphId
-- Type: BIGINT
CALL add_column_if_not_exists('kg_transform_task', 'graph_id', "BIGINT DEFAULT 1 COMMENT 'Associated graph ID'")//

-- ---- kg_transform_task: source_type ----
-- Required by: KgTransformTask.sourceType
-- Type: VARCHAR(32)
CALL add_column_if_not_exists('kg_transform_task', 'source_type', "VARCHAR(32) COMMENT 'Source file type: csv, json, xml'")//

-- ---- kg_corpus: content ----
-- Required by: KgCorpus.content (LLM extraction needs text content)
-- Type: LONGTEXT
CALL add_column_if_not_exists('kg_corpus', 'content', "LONGTEXT COMMENT 'Corpus text content for LLM extraction'")//

-- ---- kg_node_instance: update_time ----
-- Required by: KgNodeInstance.updateTime (consistency with other entities)
-- Type: DATETIME
CALL add_column_if_not_exists('kg_node_instance', 'update_time', "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time'")//

-- ---- kg_edge_instance: update_time ----
-- Required by: KgEdgeInstance.updateTime (consistency with other entities)
-- Type: DATETIME
CALL add_column_if_not_exists('kg_edge_instance', 'update_time', "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time'")//

-- ---- kg_node_instance: source_task_id ----
-- Required by: KgNodeInstance.sourceTaskId — 关联来源抽取任务
-- Type: BIGINT
CALL add_column_if_not_exists('kg_node_instance', 'source_task_id', "BIGINT COMMENT 'Source extract task ID'")//

-- ---- kg_node_instance: neo4j_id ----
-- Required by: KgNodeInstance.neo4jId — Neo4j 原生节点 ID，用于双存储关联
-- Type: BIGINT
CALL add_column_if_not_exists('kg_node_instance', 'neo4j_id', "BIGINT COMMENT 'Neo4j native node ID for dual-storage linking'")//

-- ---- kg_edge_instance: source_task_id ----
-- Required by: KgEdgeInstance.sourceTaskId — 关联来源抽取任务
-- Type: BIGINT
CALL add_column_if_not_exists('kg_edge_instance', 'source_task_id', "BIGINT COMMENT 'Source extract task ID'")//

-- ---- kg_node_instance: deleted ----
-- Required by: KgNodeInstance.deleted (logical soft-delete)
-- Type: INT
CALL add_column_if_not_exists('kg_node_instance', 'deleted', "INT DEFAULT 0 COMMENT 'Soft delete: 0=not deleted, 1=deleted'")//

-- ---- kg_edge_instance: deleted ----
-- Required by: KgEdgeInstance.deleted (logical soft-delete)
-- Type: INT
CALL add_column_if_not_exists('kg_edge_instance', 'deleted', "INT DEFAULT 0 COMMENT 'Soft delete: 0=not deleted, 1=deleted'")//

-- ---- kg_annotation_auth: annotation authorization records ----
-- Stores annotation authorization records for annotators
CREATE TABLE IF NOT EXISTS kg_annotation_auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) COMMENT 'Authorization name',
    annotator_id BIGINT COMMENT 'Annotator user ID',
    annotator_username VARCHAR(64) COMMENT 'Annotator username',
    corpus_id BIGINT COMMENT 'Associated corpus ID',
    corpus_name VARCHAR(128) COMMENT 'Corpus name',
    annotation_type VARCHAR(32) DEFAULT 'entity' COMMENT 'Annotation type: entity, relation',
    scope VARCHAR(32) COMMENT 'Authorization scope',
    status VARCHAR(32) DEFAULT 'active' COMMENT 'Status: active, revoked',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted INT DEFAULT 0 COMMENT 'Soft delete: 0=not deleted, 1=deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4//

-- ---- kg_multimodal_data: multimodal data management ----
-- Stores multimodal data (image, video, audio, document) linked to graph nodes
CREATE TABLE IF NOT EXISTS kg_multimodal_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    graph_id BIGINT COMMENT 'Associated graph ID',
    data_type VARCHAR(32) COMMENT 'Data type: image, video, audio, document, text, 3d_model',
    source_url VARCHAR(512) COMMENT 'Source URL or MinIO path',
    local_path VARCHAR(512) COMMENT 'Local storage path',
    description VARCHAR(512) COMMENT 'Data description',
    status VARCHAR(32) DEFAULT 'uploaded' COMMENT 'Status: uploaded, processing, linked, failed',
    metadata JSON COMMENT 'Additional metadata (nodeId, etc.)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted INT DEFAULT 0 COMMENT 'Soft delete: 0=not deleted, 1=deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4//

DELIMITER ;
