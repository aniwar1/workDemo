package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_corpus")
public class KgCorpus {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String status;
    private Long graphId;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
