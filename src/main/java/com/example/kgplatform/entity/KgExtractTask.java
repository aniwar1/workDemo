package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_extract_task")
public class KgExtractTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String extractType;
    private Long graphId;
    private Long modelId;
    private String sourceType;
    private Long sourceId;
    private String status;
    private Integer extractedCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
