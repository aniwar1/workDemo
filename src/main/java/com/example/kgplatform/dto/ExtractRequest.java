package com.example.kgplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractRequest {
    /** 抽取类型: entity=仅实体, relation=仅关系, all=全部 */
    private String extractType = "all";
    /** 文本语言: zh=中文, en=英文 */
    private String language = "zh";
    /** 使用的LLM模型名称，留空则使用配置默认模型 */
    private String model;
    /** 自定义schema约束，如 "人物,公司,产品,地点" */
    private String schema;
    /** 待抽取的文本内容 */
    private String text;
    /** 目标图谱ID，saveToGraph=true时必填 */
    private Long graphId;
    /** 是否保存到图谱（同时写Neo4j和MySQL） */
    private Boolean saveToGraph = false;
}
