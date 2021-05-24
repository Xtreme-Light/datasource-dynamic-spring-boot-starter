package com.spring.dynamic.datasource.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(DynamicDatasourceProperties.class)
@ConditionalOnProperty(
                 prefix = "spring.dynamic.datasource",
                 name = "enable",
                 havingValue = "true"
         )
public class DynamicDatasourceConfig {
    @Autowired
    private DynamicDatasourceProperties dynamicDatasourceProperties;
}
