package com.example.kgplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kg_train_task")
public class KgTrainTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String modelType;
    private Long corpusId;
    private String config;
    private String status;
    private Float accuracy;
    @TableField("precision_col")
    private Float precisionVal;
    private Float recall;
    private Float f1Score;
    private Long trainTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
