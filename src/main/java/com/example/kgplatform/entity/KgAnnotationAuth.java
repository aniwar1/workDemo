package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_annotation_auth")
public class KgAnnotationAuth {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long annotatorId;

    private String annotatorUsername;

    private Long corpusId;

    private String corpusName;

    private String annotationType;

    private String scope;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
