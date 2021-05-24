package com.spring.dynamic.datasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.dynamic.datasource")
public class DynamicDatasourceProperties {
    private boolean enable;
}
