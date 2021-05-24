package com.spring.dynamic.datasource.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setDataSource(String dataSourceType) {
        if (log.isDebugEnabled()) {
            log.debug("切换数据源到{}",dataSourceType);
        }
        CONTEXT_HOLDER.set(dataSourceType);
    }

    public static String getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
}
