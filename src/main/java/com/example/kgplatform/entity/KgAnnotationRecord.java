package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_annotation_record")
public class KgAnnotationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long corpusId;
    private Long annotatorId;
    private String content;
    private String annotation;
    private String entityTypes;
    private String relationTypes;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
