package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_model")
public class KgModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    @JsonProperty("schema")
    @TableField("schema_col")
    private String modelSchema;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
