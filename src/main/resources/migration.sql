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

DELIMITER ;
