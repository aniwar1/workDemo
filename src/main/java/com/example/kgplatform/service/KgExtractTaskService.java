package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageQueryUtil;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgExtractTask;
import com.example.kgplatform.mapper.KgExtractTaskMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KgExtractTaskService extends ServiceImpl<KgExtractTaskMapper, KgExtractTask> {

    private static final Map<Long, Thread> runningTasks = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public PageResult<KgExtractTask> pageQuery(PageQuery query) {
        return PageQueryUtil.pagedQuery(this, query, wrapper -> {
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                wrapper.like(KgExtractTask::getName, query.getKeyword());
            }
            wrapper.orderByDesc(KgExtractTask::getCreateTime);
        });
    }

    @Async
    public void startExtractAsync(Long taskId) {
        KgExtractTask task = getById(taskId);
        if (task == null) return;

        task.setStatus("running");
        updateById(task);

        try {
            Thread.sleep(2000 + random.nextInt(3000));

            task.setExtractedCount(10 + random.nextInt(90));
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

    public Map<String, Object> getExtractResult(Long taskId) {
        KgExtractTask task = getById(taskId);
        if (task == null) return null;
        return Map.of(
                "task", task,
                "extractedCount", task.getExtractedCount() != null ? task.getExtractedCount() : 0
        );
    }
}
