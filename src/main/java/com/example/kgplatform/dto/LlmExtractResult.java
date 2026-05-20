package com.example.kgplatform.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class LlmExtractResult {
    private List<Entity> entities;
    private List<Relation> relations;

    @Data
    public static class Entity {
        private String name;
        private String type;
        private Map<String, Object> attributes;
    }

    @Data
    public static class Relation {
        private String source;
        private String target;
        private String type;
        private Map<String, Object> attributes;
    }
}
