package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_node_instance")
public class KgNodeInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long graphId;
    private String nodeName;
    private String nodeType;
    private String properties;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
