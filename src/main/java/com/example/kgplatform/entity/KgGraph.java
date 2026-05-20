package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_graph")
public class KgGraph {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String projectManager;
    private String status;
    private Long modelId;
    private String modelName;
    private String storageEngine;
    private Boolean storageEngineConfigured;
    private Boolean graphSpaceCreated;
    private Integer nodeCount;
    private Integer edgeCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
