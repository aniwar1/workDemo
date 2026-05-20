package com.example.kgplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.kgplatform.common.PageQuery;
import com.example.kgplatform.common.PageResult;
import com.example.kgplatform.entity.KgGraph;
import com.example.kgplatform.entity.KgModel;
import com.example.kgplatform.mapper.KgGraphMapper;
import com.example.kgplatform.mapper.KgModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KgGraphService extends ServiceImpl<KgGraphMapper, KgGraph> {

    private final KgModelMapper kgModelMapper;

    public KgGraphService(KgGraphMapper kgGraphMapper, KgModelMapper kgModelMapper) {
        super();
        this.kgModelMapper = kgModelMapper;
    }

    public PageResult<KgGraph> pageQuery(PageQuery query) {
        Page<KgGraph> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KgGraph> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(KgGraph::getName, query.getKeyword());
        }
        wrapper.orderByDesc(KgGraph::getId);
        Page<KgGraph> result = page(page, wrapper);

        List<KgGraph> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> modelIds = records.stream()
                    .map(KgGraph::getModelId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, String> modelNameMap = new HashMap<>();
            if (!modelIds.isEmpty()) {
                List<KgModel> models = kgModelMapper.selectBatchIds(modelIds);
                modelNameMap = models.stream()
                        .collect(Collectors.toMap(KgModel::getId, KgModel::getName));
            }

            for (KgGraph graph : records) {
                graph.setModelName(modelNameMap.get(graph.getModelId()));
            }
        }

        return PageResult.of(result.getTotal(), records);
    }
}
