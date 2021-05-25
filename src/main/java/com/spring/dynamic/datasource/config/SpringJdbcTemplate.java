package com.spring.dynamic.datasource.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;

public class SpringJdbcTemplate extends JdbcTemplate {
    @Override
    public DataSource getDataSource() {
        DynamicDataSource router =  (DynamicDataSource) super.getDataSource();
        assert router != null;
        Map<Object, DataSource> resolvedDataSources = router.getResolvedDataSources();
        String dataSource = DynamicDataSourceContextHolder.getDataSource();
        if (StringUtils.hasLength(dataSource)) {
            return resolvedDataSources.get(dataSource);
        }else {
            return router.getResolvedDefaultDataSource();
        }
    }

    public SpringJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

}
