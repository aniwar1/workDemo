package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_annotation_task")
public class KgAnnotationTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long corpusId;
    private Long assigneeId;
    private String status;
    private Integer totalCount;
    private Integer completedCount;
    private String annotationType;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
