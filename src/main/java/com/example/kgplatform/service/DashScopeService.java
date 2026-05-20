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

        String prompt = buildPrompt(text, schema);
        String rawResponse = callLlm(prompt);
        return parseResponse(rawResponse);
    }

    private String buildPrompt(String text, String schema) {
        if (schema != null && !schema.isBlank()) {
            return String.format("""
                你是一个知识图谱抽取专家。请从以下文本中抽取实体和关系，并以严格的JSON格式返回。

                ## 文本内容
                %s

                ## 实体类型定义（可参考，可自行发现更多合理的实体类型）
                %s

                ## 输出格式要求
                必须返回以下JSON结构，不要包含任何其他内容：
                {
                    "entities": [
                        {"name": "实体名称", "type": "实体类型", "attributes": {}}
                    ],
                    "relations": [
                        {"source": "源实体名", "target": "目标实体名", "type": "关系类型", "attributes": {}}
                    ]
                }

                如果没有找到任何实体或关系，返回空数组 "entities": [] 和 "relations": []。
                """, text, schema);
        }
        return String.format("""
            你是一个知识图谱抽取专家。请从以下文本中抽取实体和关系，并以严格的JSON格式返回。

            ## 文本内容
            %s

            ## 输出格式要求
            必须返回以下JSON结构，不要包含任何其他内容：
            {
                "entities": [
                    {"name": "实体名称", "type": "实体类型", "attributes": {}}
                ],
                "relations": [
                    {"source": "源实体名", "target": "目标实体名", "type": "关系类型", "attributes": {}}
                ]
            }

            常见实体类型包括：人物、产品、公司、地点、事件、组织、技术等。
            常见关系类型包括：属于、创立、生产、合作、位于、相关等。
            如果没有找到任何实体或关系，返回空数组 "entities": [] 和 "relations": []。
            """, text);
    }

    private String callLlm(String prompt) {
        String url = properties.getBaseUrl() + "/chat/completions";

        String systemMsg = "你是一个严格遵循JSON格式的知识图谱抽取助手。只需输出JSON，不要有其他任何文字。";

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMsg),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.1,
                    "max_tokens", 2048
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
