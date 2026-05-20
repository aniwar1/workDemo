package com.example.kgplatform.controller;

import com.example.kgplatform.common.R;
import com.example.kgplatform.service.KgGraphService;
import com.example.kgplatform.service.KgModelService;
import com.example.kgplatform.service.KgTrainTaskService;
import com.example.kgplatform.service.KgCorpusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "系统首页")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final KgGraphService kgGraphService;
    private final KgModelService kgModelService;
    private final KgTrainTaskService kgTrainTaskService;
    private final KgCorpusService kgCorpusService;

    public HomeController(KgGraphService kgGraphService, KgModelService kgModelService,
                          KgTrainTaskService kgTrainTaskService, KgCorpusService kgCorpusService) {
        this.kgGraphService = kgGraphService;
        this.kgModelService = kgModelService;
        this.kgTrainTaskService = kgTrainTaskService;
        this.kgCorpusService = kgCorpusService;
    }

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("graphCount", kgGraphService.count());
        data.put("modelCount", kgModelService.count());
        data.put("trainTaskCount", kgTrainTaskService.count());
        data.put("corpusCount", kgCorpusService.count());
        data.put("graphTrend", new int[]{12, 19, 15, 22, 18, 25, 30});
        data.put("taskTrend", new int[]{8, 12, 10, 15, 14, 18, 20});
        return R.ok(data);
    }
}
