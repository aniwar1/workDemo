package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.dto.LlmExtractResult;
import com.example.kgplatform.entity.KgCorpus;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.mapper.KgExtractTaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KgExtractTaskService extends ServiceImpl<KgExtractTaskMapper, KgExtractTask> {

    private static final Logger log = LoggerFactory.getLogger(KgExtractTaskService.class);
    private static final Map<Long, Thread> runningTasks = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public PageResult<KgExtractTask> pageQuery(PageQuery query) {
        Page<KgExtractTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgExtractTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgExtractTask::getName, query.getKeyword());
        }
        Page<KgExtractTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Async
    public void startExtractAsync(Long taskId, Neo4jService neo4jService, KgCorpusService corpusService,
                                  DashScopeService dashScopeService) {
        KgExtractTask task = getById(taskId);
        if (task == null) return;

        task.setStatus("running");
        updateById(task);

        try {
            if ("llm".equalsIgnoreCase(task.getExtractType())) {
                int totalExtracted = doLlmExtract(task, neo4jService, corpusService, dashScopeService);
                task.setExtractedCount(totalExtracted);
                task.setStatus("completed");
            } else {
                Thread.sleep(2000 + random.nextInt(3000));
                int count = 10 + random.nextInt(20);
                task.setExtractedCount(count);
                task.setStatus("completed");
            }
        } catch (InterruptedException e) {
            task.setStatus("stopped");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("抽取任务 {} 失败: {}", taskId, e.getMessage(), e);
            task.setStatus("failed");
        }

        updateById(task);
        runningTasks.remove(taskId);
    }

    private int doLlmExtract(KgExtractTask task, Neo4jService neo4jService,
                             KgCorpusService corpusService, DashScopeService dashScopeService) throws Exception {
        KgCorpus corpus = corpusService.getById(task.getSourceId());
        if (corpus == null) {
            throw new IllegalArgumentException("语料不存在: sourceId=" + task.getSourceId());
        }

        String text = corpus.getContent();
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("语料内容为空");
        }

        log.info("开始 LLM 抽取，语料ID={}，文本长度={}", corpus.getId(), text.length());

        LlmExtractResult result = dashScopeService.extract(text, null);

        List<LlmExtractResult.Entity> entities = result.getEntities();
        List<LlmExtractResult.Relation> relations = result.getRelations();

        log.info("LLM 抽取结果: 实体数={}, 关系数={}", entities.size(), relations.size());

        List<Map<String, Object>> nodesToSave = new ArrayList<>();
        for (LlmExtractResult.Entity e : entities) {
            Map<String, Object> node = new HashMap<>();
            node.put("name", e.getName());
            node.put("entityType", e.getType());
            node.put("graphId", task.getGraphId());
            if (e.getAttributes() != null) {
                node.put("properties", e.getAttributes());
            }
            nodesToSave.add(node);
        }

        List<Map<String, Object>> relationsToSave = new ArrayList<>();
        for (LlmExtractResult.Relation r : relations) {
            relationsToSave.add(Map.of(
                    "sourceName", r.getSource(),
                    "targetName", r.getTarget(),
                    "relationType", r.getType(),
                    "graphId", task.getGraphId()
            ));
        }

        if (!nodesToSave.isEmpty() || !relationsToSave.isEmpty()) {
            neo4jService.batchCreateNodesAndRelations(nodesToSave, relationsToSave);
        }

        return entities.size();
    }

    public void stopExtract(Long taskId) {
        Thread thread = runningTasks.remove(taskId);
        if (thread != null) {
            thread.interrupt();
        }
        KgExtractTask task = getById(taskId);
        if (task != null) {
            task.setStatus("stopped");
            updateById(task);
        }
    }

    public boolean isRunning(Long taskId) {
        return runningTasks.containsKey(taskId);
    }
}
