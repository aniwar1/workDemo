package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_edge_instance")
public class KgEdgeInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long graphId;
    private Long sourceNodeId;
    private Long targetNodeId;
    private String relationType;
    private String properties;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
