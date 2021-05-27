package com.spring.dynamic.datasource.config;

import com.spring.dynamic.datasource.annotation.CurDatasource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.PriorityOrdered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConditionalOnBean(DataSource.class)
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@Configuration
@EnableConfigurationProperties(DynamicDatasourceProperties.class)
@ConditionalOnProperty(
                 prefix = "spring.dynamic.datasource",
                 name = "enable",
                 havingValue = "true"
         )
@Slf4j
@Aspect
public class DynamicDatasourceConfig implements PriorityOrdered, ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private  DynamicDatasourceProperties dynamicDatasourceProperties;

    @Around("@annotation(com.spring.dynamic.datasource.annotation.CurDatasource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        CurDatasource dataSourceAnnotation = method.getAnnotation(CurDatasource.class);
        if (dataSourceAnnotation != null) {
            String value = dataSourceAnnotation.value();
            if (log.isDebugEnabled()) {
                log.debug("切换数据源到{}", value);
            }
            DynamicDataSourceContextHolder.setDataSource(value);
        }
        try{
            return point.proceed();
        }finally {
            DynamicDataSourceContextHolder.clearDataSource();
        }
    }

    @Override
    public int getOrder() {
        //保证事务等切面先执行
        return Integer.MIN_VALUE;
    }

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public SpringJdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new SpringJdbcTemplate(dataSource);
    }
    @Bean
    @Primary
    public DataSource dataSource() {
        String defaultDatasource = dynamicDatasourceProperties.getDefaultDatasource();
        DataSource bean = (DataSource)applicationContext.getBean(defaultDatasource);
        List<String> datasourceList = dynamicDatasourceProperties.getDatasourceList();
        Map<Object, Object> maps = new HashMap<>();
        if (CollectionUtils.isEmpty(datasourceList)) {
            String[] beanNamesForType = applicationContext.getBeanNamesForType(DataSource.class);
            for (String name : beanNamesForType) {
                maps.putIfAbsent(name, applicationContext.getBean(name));
            }
        }else {
            for (String name : datasourceList) {
                maps.putIfAbsent(name, applicationContext.getBean(name));
            }
        }
        return new DynamicDataSource(bean,maps);
    }
    //TODO  when do this,dynamicDatasourceProperties is null
/*    @Bean
    public BeanDefinitionRegistryPostProcessor dynamicDatasourceManagerRegister() {
        DynamicDataSourceManagerRegister dynamicDataSourceManagerRegister = new DynamicDataSourceManagerRegister();
        dynamicDataSourceManagerRegister.setDynamicDatasourceProperties(dynamicDatasourceProperties);
        dynamicDataSourceManagerRegister.setApplicationContext(applicationContext);
        return dynamicDataSourceManagerRegister;
    }*/


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
