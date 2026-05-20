package com.example.kgplatform.service;

import com.example.kgplatform.config.DashScopeProperties;
import com.example.kgplatform.dto.LlmExtractResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DashScopeService {

    private static final Logger log = LoggerFactory.getLogger(DashScopeService.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final DashScopeProperties properties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public DashScopeService(DashScopeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public LlmExtractResult extract(String text, String schema) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("待抽取文本不能为空");
        }

        String prompt = buildPrompt(text, schema, "all", "zh");
        String rawResponse = callLlm(prompt, null);
        return parseResponse(rawResponse);
    }

    public LlmExtractResult extractDirect(String text, String extractType, String language, String modelOverride) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("待抽取文本不能为空");
        }

        String prompt = buildPrompt(text, null, extractType, language);
        String rawResponse = callLlm(prompt, modelOverride);
        return parseResponse(rawResponse);
    }

    private String buildPrompt(String text, String schema, String extractType, String language) {
        String langInstruction = "zh".equalsIgnoreCase(language)
                ? "使用中文输出实体类型和关系类型。"
                : "Output entity types and relation types in English.";

        String extractInstruction;
        switch (extractType == null ? "all" : extractType) {
            case "entity" -> extractInstruction = "## 抽取要求\n只抽取实体，不要抽取关系。返回结果中 relations 字段设为空数组。";
            case "relation" -> extractInstruction = "## 抽取要求\n只抽取关系（仅在能明确判断源实体和目标实体时），实体可忽略。返回结果中 entities 字段设为空数组。";
            default -> extractInstruction = "## 抽取要求\n同时抽取实体和关系。";
        }

        String schemaSection = (schema != null && !schema.isBlank())
                ? "## 实体类型定义（可参考，可自行发现更多合理的实体类型）\n" + schema + "\n"
                : "";

        String entityHints = "zh".equalsIgnoreCase(language)
                ? "常见实体类型包括：人物、产品、公司、地点、事件、组织、技术等。\n常见关系类型包括：属于、创立、生产、合作、位于、相关等。"
                : "Common entity types: Person, Product, Company, Location, Event, Organization, Technology, etc.\nCommon relation types: belongs_to, founded_by, produces, cooperates_with, located_in, related_to, etc.";

        return String.format("""
            You are a knowledge graph extraction expert. Extract entities and relations from the given text and return them in strict JSON format.

            ## Text Content
            %s

            %s
            %s

            ## Output Format (mandatory JSON, no other text)
            {
                "entities": [
                    {"name": "entity name", "type": "entity type", "attributes": {}}
                ],
                "relations": [
                    {"source": "source entity name", "target": "target entity name", "type": "relation type", "attributes": {}}
                ]
            }

            %s
            %s
            If no entities or relations are found, return "entities": [] and "relations": [].
            """,
                text, schemaSection, extractInstruction, langInstruction, entityHints);
    }

    private String callLlm(String prompt, String modelOverride) {
        String url = properties.getBaseUrl() + "/chat/completions";

        String systemMsg = "You are a knowledge graph extraction assistant that strictly follows JSON format. Only output JSON, no other text.";

        String model = (modelOverride != null && !modelOverride.isBlank()) ? modelOverride : properties.getModel();

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMsg),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 4096
            ));
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败: " + e.getMessage(), e);
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + properties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, JSON))
                .build();

        int retries = 0;
        while (retries <= properties.getMaxRetries()) {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "无响应体";
                    log.warn("LLM API 请求失败（第{}次重试），HTTP {}: {}", retries + 1, response.code(), errorBody);
                    retries++;
                    continue;
                }

                String body = response.body() != null ? response.body().string() : "";
                return extractContent(body);

            } catch (IOException e) {
                log.warn("LLM API 调用 IO 异常（第{}次重试）: {}", retries + 1, e.getMessage());
                retries++;
            }
        }

        throw new RuntimeException("LLM API 调用失败，已达到最大重试次数（" + properties.getMaxRetries() + "）");
    }

    private String extractContent(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("error")) {
            String errMsg = root.get("error").asText();
            throw new RuntimeException("LLM API 返回错误: " + errMsg);
        }

        JsonNode choices = root.path("choices");
        if (choices.isMissingNode() || !choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException("LLM 响应中无 choices 内容");
        }

        JsonNode message = choices.get(0).path("message");
        if (message.isMissingNode()) {
            throw new RuntimeException("LLM 响应中无 message 内容");
        }

        return message.path("content").asText("");
    }

    private LlmExtractResult parseResponse(String raw) {
        try {
            String jsonContent = extractJsonContent(raw);
            LlmExtractResult result = objectMapper.readValue(jsonContent, LlmExtractResult.class);
            if (result.getEntities() == null) result.setEntities(new ArrayList<>());
            if (result.getRelations() == null) result.setRelations(new ArrayList<>());
            return result;
        } catch (Exception e) {
            log.error("解析 LLM 响应失败，原始内容: {}", raw, e);
            throw new RuntimeException("解析 LLM 响应失败: " + e.getMessage(), e);
        }
    }

    private String extractJsonContent(String content) {
        if (content == null) return "{}";
        String trimmed = content.trim();
        if (trimmed.startsWith("```json") || trimmed.startsWith("```")) {
            int start = trimmed.indexOf("{");
            int end = trimmed.lastIndexOf("}");
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        int start = trimmed.indexOf("{");
        int end = trimmed.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return "{}";
    }
}
