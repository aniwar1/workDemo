package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.mapper.KgExtractTaskMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KgExtractTaskService extends ServiceImpl<KgExtractTaskMapper, KgExtractTask> {

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
    public void startExtractAsync(Long taskId, Neo4jService neo4jService, KgCorpusService corpusService) {
        KgExtractTask task = getById(taskId);
        if (task == null) return;

        task.setStatus("running");
        updateById(task);

        try {
            Thread.sleep(2000 + random.nextInt(3000));

            int count = 10 + random.nextInt(20);
            task.setExtractedCount(count);
            task.setStatus("completed");
        } catch (InterruptedException e) {
            task.setStatus("stopped");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            task.setStatus("failed");
        }

        updateById(task);
        runningTasks.remove(taskId);
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
