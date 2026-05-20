package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_multimodal_data")
public class KgMultimodalData {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long graphId;

    private String dataType;

    private String sourceUrl;

    private String localPath;

    private String description;

    private String status;

    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
