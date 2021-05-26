package com.spring.dynamic.datasource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.dynamic.datasource")
@Data
public class DynamicDatasourceProperties {
    private boolean enable;
    private String defaultDatasource;
    private List<String> datasourceList;
    private String transactionManagerSuffix;

}
