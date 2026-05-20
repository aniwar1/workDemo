package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgTrainTask;
import com.example.kgplatform.mapper.KgTrainTaskMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KgTrainTaskService extends ServiceImpl<KgTrainTaskMapper, KgTrainTask> {

    private static final Map<Long, Thread> runningTasks = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public PageResult<KgTrainTask> pageQuery(PageQuery query) {
        Page<KgTrainTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgTrainTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgTrainTask::getName, query.getKeyword());
        }
        wrapper.orderByDesc(KgTrainTask::getCreateTime);
        Page<KgTrainTask> result = page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    @Async
    public void startTrainAsync(Long taskId) {
        KgTrainTask task = getById(taskId);
        if (task == null) return;

        task.setStatus("running");
        updateById(task);

        try {
            long startTime = System.currentTimeMillis();
            Thread.sleep(3000 + random.nextInt(4000));
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;

            task.setAccuracy(0.75f + random.nextFloat() * 0.20f);
            task.setPrecisionVal(0.72f + random.nextFloat() * 0.22f);
            task.setRecall(0.70f + random.nextFloat() * 0.23f);
            task.setF1Score((task.getPrecisionVal() + task.getRecall()) / 2);
            task.setTrainTime(elapsed);
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

    public void stopTrain(Long taskId) {
        Thread thread = runningTasks.remove(taskId);
        if (thread != null) {
            thread.interrupt();
        }
        KgTrainTask task = getById(taskId);
        if (task != null) {
            task.setStatus("stopped");
            updateById(task);
        }
    }

    public String getTrainLogs(Long taskId) {
        KgTrainTask task = getById(taskId);
        if (task == null) return "";
        StringBuilder logs = new StringBuilder();
        logs.append("=== 训练任务日志 ===\n");
        logs.append("任务名称: ").append(task.getName()).append("\n");
        logs.append("模型类型: ").append(task.getModelType()).append("\n");
        logs.append("状态: ").append(task.getStatus()).append("\n");
        logs.append("语料ID: ").append(task.getCorpusId()).append("\n");
        if (task.getAccuracy() != null) {
            logs.append("准确率: ").append(String.format("%.4f", task.getAccuracy())).append("\n");
            logs.append("精确率: ").append(String.format("%.4f", task.getPrecisionVal())).append("\n");
            logs.append("召回率: ").append(String.format("%.4f", task.getRecall())).append("\n");
            logs.append("F1分数: ").append(String.format("%.4f", task.getF1Score())).append("\n");
            logs.append("训练时长: ").append(task.getTrainTime()).append("s\n");
        }
        logs.append("创建时间: ").append(task.getCreateTime()).append("\n");
        logs.append("====================");
        return logs.toString();
    }
}
