package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgTransformTask;
import com.example.kgplatform.entity.KgNodeInstance;
import com.example.kgplatform.entity.KgEdgeInstance;
import com.example.kgplatform.mapper.KgTransformTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Service
public class KgTransformTaskService extends ServiceImpl<KgTransformTaskMapper, KgTransformTask> {

    private final KgNodeInstanceService nodeInstanceService;
    private final KgEdgeInstanceService edgeInstanceService;
    private final Neo4jService neo4jService;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    @Value("${file.upload-path:uploads}")
    private String uploadPath;

    public KgTransformTaskService(KgNodeInstanceService nodeInstanceService,
                                  KgEdgeInstanceService edgeInstanceService,
                                  Neo4jService neo4jService) {
        this.nodeInstanceService = nodeInstanceService;
        this.edgeInstanceService = edgeInstanceService;
        this.neo4jService = neo4jService;
        this.objectMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    public PageResult<KgTransformTask> pageQuery(PageQuery query) {
        Page<KgTransformTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgTransformTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgTransformTask::getName, query.getKeyword());
        }
        wrapper.orderByDesc(KgTransformTask::getCreateTime);
        Page<KgTransformTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    public Map<String, Object> executeTransform(Long taskId) {
        KgTransformTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        task.setStatus("running");
        updateById(task);

        Map<String, Object> result = new HashMap<>();
        int recordCount = 0;

        try {
            String inputPath = task.getInputPath();
            if (inputPath == null || inputPath.isEmpty()) {
                throw new RuntimeException("输入路径为空");
            }

            String content = readFileContent(inputPath);
            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Object>> relations = new ArrayList<>();

            if ("csv".equalsIgnoreCase(task.getSourceType())) {
                recordCount = parseCsvAndExtract(content, nodes, relations, task.getGraphId() != null ? task.getGraphId() : 1L);
            } else if ("json".equalsIgnoreCase(task.getSourceType())) {
                recordCount = parseJsonAndExtract(content, nodes, relations, task.getGraphId() != null ? task.getGraphId() : 1L);
            } else if ("xml".equalsIgnoreCase(task.getSourceType())) {
                recordCount = parseXmlAndExtract(content, nodes, relations, task.getGraphId() != null ? task.getGraphId() : 1L);
            } else {
                throw new RuntimeException("不支持的源格式: " + task.getSourceType());
            }

            for (Map<String, Object> node : nodes) {
                KgNodeInstance ni = new KgNodeInstance();
                ni.setGraphId(((Number) node.get("graphId")).longValue());
                ni.setNodeName((String) node.get("name"));
                ni.setNodeType((String) node.getOrDefault("type", "实体"));
                ni.setProperties(objectMapper.writeValueAsString(node.getOrDefault("properties", new HashMap<>())));
                nodeInstanceService.save(ni);
            }

            for (Map<String, Object> rel : relations) {
                KgEdgeInstance ei = new KgEdgeInstance();
                ei.setGraphId(((Number) rel.get("graphId")).longValue());
                ei.setSourceNodeId(((Number) rel.get("sourceId")).longValue());
                ei.setTargetNodeId(((Number) rel.get("targetId")).longValue());
                ei.setRelationType((String) rel.get("relationType"));
                ei.setProperties(objectMapper.writeValueAsString(rel.getOrDefault("properties", new HashMap<>())));
                edgeInstanceService.save(ei);
            }

            if ("kg_json".equals(task.getTargetFormat())) {
                writeKgJson(nodes, relations, task.getOutputPath());
            }

            task.setRecordCount(recordCount);
            task.setStatus("completed");
            result.put("nodes", nodes.size());
            result.put("relations", relations.size());
        } catch (Exception e) {
            task.setStatus("failed");
            result.put("error", e.getMessage());
        }

        updateById(task);
        return result;
    }

    private String readFileContent(String inputPath) throws IOException {
        if (inputPath.startsWith("http") || inputPath.contains("minio")) {
            try (InputStream in = neo4jService.getClass().getResourceAsStream("/" + inputPath)) {
                if (in == null) {
                    File f = new File(uploadPath, inputPath);
                    if (f.exists()) {
                        return Files.readString(f.toPath(), StandardCharsets.UTF_8);
                    }
                    return "";
                }
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        File f = new File(uploadPath, inputPath);
        if (f.exists()) {
            return Files.readString(f.toPath(), StandardCharsets.UTF_8);
        }
        return "";
    }

    private int parseCsvAndExtract(String content, List<Map<String, Object>> nodes,
                                    List<Map<String, Object>> relations, Long graphId) throws Exception {
        String[] lines = content.split("\n");
        if (lines.length < 2) return 0;
        String[] headers = lines[0].split(",");
        int count = 0;
        Map<String, Long> nodeNameToId = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(",");
            if (parts.length < 2) continue;
            String subject = parts[0].trim().replaceAll("^\"|\"$", "");
            String predicate = parts.length > 2 ? parts[1].trim().replaceAll("^\"|\"$", "") : "关联";
            String obj = parts.length > 2 ? parts[2].trim().replaceAll("^\"|\"$", "") : parts[1].trim().replaceAll("^\"|\"$", "");

            if (!nodeNameToId.containsKey(subject)) {
                long id = i;
                nodeNameToId.put(subject, id);
                nodes.add(Map.of("name", subject, "type", "实体", "graphId", graphId));
            }
            if (!nodeNameToId.containsKey(obj)) {
                long id = lines.length + i;
                nodeNameToId.put(obj, id);
                nodes.add(Map.of("name", obj, "type", "实体", "graphId", graphId));
            }
            relations.add(Map.of(
                    "sourceId", nodeNameToId.get(subject),
                    "targetId", nodeNameToId.get(obj),
                    "relationType", predicate,
                    "graphId", graphId
            ));
            count++;
        }
        return count;
    }

    private int parseJsonAndExtract(String content, List<Map<String, Object>> nodes,
                                     List<Map<String, Object>> relations, Long graphId) throws Exception {
        JsonNode root = objectMapper.readTree(content);
        int count = 0;
        Map<String, Long> nodeNameToId = new HashMap<>();

        if (root.isArray()) {
            for (JsonNode item : root) {
                String subject = item.has("subject") ? item.get("subject").asText() : (item.has("s") ? item.get("s").asText() : "");
                String predicate = item.has("predicate") ? item.get("predicate").asText() : (item.has("p") ? item.get("p").asText() : "关联");
                String obj = item.has("object") ? item.get("object").asText() : (item.has("o") ? item.get("o").asText() : "");

                if (subject.isEmpty() || obj.isEmpty()) continue;

                if (!nodeNameToId.containsKey(subject)) {
                    long id = nodeNameToId.size() + 1;
                    nodeNameToId.put(subject, id);
                    nodes.add(Map.of("name", subject, "type", "实体", "graphId", graphId));
                }
                if (!nodeNameToId.containsKey(obj)) {
                    long id = nodeNameToId.size() + 1;
                    nodeNameToId.put(obj, id);
                    nodes.add(Map.of("name", obj, "type", "实体", "graphId", graphId));
                }
                relations.add(Map.of(
                        "sourceId", nodeNameToId.get(subject),
                        "targetId", nodeNameToId.get(obj),
                        "relationType", predicate,
                        "graphId", graphId
                ));
                count++;
            }
        } else if (root.has("triples")) {
            JsonNode triples = root.get("triples");
            for (JsonNode triple : triples) {
                String s = triple.get("s").asText();
                String p = triple.get("p").asText();
                String o = triple.get("o").asText();

                if (!nodeNameToId.containsKey(s)) {
                    long id = nodeNameToId.size() + 1;
                    nodeNameToId.put(s, id);
                    nodes.add(Map.of("name", s, "type", "实体", "graphId", graphId));
                }
                if (!nodeNameToId.containsKey(o)) {
                    long id = nodeNameToId.size() + 1;
                    nodeNameToId.put(o, id);
                    nodes.add(Map.of("name", o, "type", "实体", "graphId", graphId));
                }
                relations.add(Map.of(
                        "sourceId", nodeNameToId.get(s),
                        "targetId", nodeNameToId.get(o),
                        "relationType", p,
                        "graphId", graphId
                ));
                count++;
            }
        }
        return count;
    }

    private int parseXmlAndExtract(String content, List<Map<String, Object>> nodes,
                                    List<Map<String, Object>> relations, Long graphId) throws Exception {
        JsonNode root = xmlMapper.readTree(content);
        int count = 0;
        Map<String, Long> nodeNameToId = new HashMap<>();

        if (root.isArray()) {
            for (JsonNode item : root) {
                String subject = item.has("subject") ? item.get("subject").asText() : "";
                String predicate = item.has("predicate") ? item.get("predicate").asText() : "关联";
                String obj = item.has("object") ? item.get("object").asText() : "";

                if (subject.isEmpty() || obj.isEmpty()) continue;

                if (!nodeNameToId.containsKey(subject)) {
                    long id = nodeNameToId.size() + 1;
                    nodeNameToId.put(subject, id);
                    nodes.add(Map.of("name", subject, "type", "实体", "graphId", graphId));
                }
                if (!nodeNameToId.containsKey(obj)) {
                    long id = nodeNameToId.size() + 1;
                    nodeNameToId.put(obj, id);
                    nodes.add(Map.of("name", obj, "type", "实体", "graphId", graphId));
                }
                relations.add(Map.of(
                        "sourceId", nodeNameToId.get(subject),
                        "targetId", nodeNameToId.get(obj),
                        "relationType", predicate,
                        "graphId", graphId
                ));
                count++;
            }
        }
        return count;
    }

    private void writeKgJson(List<Map<String, Object>> nodes, List<Map<String, Object>> relations, String outputPath) throws IOException {
        Map<String, Object> kgJson = new HashMap<>();
        kgJson.put("nodes", nodes);
        kgJson.put("relations", relations);
        if (outputPath != null && !outputPath.isEmpty()) {
            File outFile = new File(uploadPath, outputPath);
            outFile.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outFile, kgJson);
        }
    }
}
