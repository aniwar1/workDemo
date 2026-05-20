package com.example.kgplatform.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.util.StringUtils;

public class PageQueryUtil {

    public static <T> PageResult<T> pagedQuery(IService<T> service, PageQuery query,
                                                java.util.function.Consumer<LambdaQueryWrapper<T>> wrapperConfig) {
        Page<T> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        if (wrapperConfig != null) {
            wrapperConfig.accept(wrapper);
        }
        Page<T> result = service.page(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    public static <T> PageResult<T> pagedQueryWithKeyword(IService<T> service, PageQuery query,
                                                          java.util.function.BiFunction<LambdaQueryWrapper<T>, String, LambdaQueryWrapper<T>> keywordAdder) {
        return pagedQuery(service, query, wrapper -> {
            if (StringUtils.hasText(query.getKeyword()) && keywordAdder != null) {
                keywordAdder.apply(wrapper, query.getKeyword());
            }
        });
    }
}
