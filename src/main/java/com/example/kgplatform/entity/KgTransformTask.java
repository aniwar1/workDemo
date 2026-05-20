package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_transform_task")
public class KgTransformTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long graphId;
    private String sourceType;
    private String targetFormat;
    private String inputPath;
    private String outputPath;
    private String status;
    private Integer recordCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
